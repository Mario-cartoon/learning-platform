package com.example.learningplatform.assignment.service;

import com.example.learningplatform.assignment.dto.GradeRequest;
import com.example.learningplatform.assignment.dto.SubmissionRequest;
import com.example.learningplatform.assignment.dto.SubmissionResponse;
import com.example.learningplatform.assignment.entity.Assignment;
import com.example.learningplatform.assignment.entity.Submission;
import com.example.learningplatform.assignment.event.SubmissionGradedEvent;
import com.example.learningplatform.assignment.repository.AssignmentRepository;
import com.example.learningplatform.assignment.repository.SubmissionRepository;
import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.enrollment.service.EnrollmentPolicy;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;
import com.example.learningplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EnrollmentPolicy enrollmentPolicy;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SubmissionResponse submit(Long assignmentId, SubmissionRequest request) {
        Assignment assignment = assignmentRepository.getOrThrow(assignmentId);
        User student = userRepository.getOrThrow(request.studentId());
        student.requireRole(Role.STUDENT);
        enrollmentPolicy.ensureEnrolled(student.getId(), assignment.resolveCourse().getId());

        if (submissionRepository.existsByStudentIdAndAssignmentId(student.getId(), assignmentId)) {
            throw new ConflictException("Студент уже отправил решение по этому заданию");
        }
        if (assignment.isOverdue(LocalDateTime.now())) {
            throw new BusinessRuleException("Срок сдачи задания истёк " + assignment.getDueDate());
        }

        Submission saved = submissionRepository.save(new Submission(assignment, student, request.content()));
        log.info("Студент id={} отправил решение id={} по заданию id={}", student.getId(), saved.getId(), assignmentId);
        return SubmissionResponse.from(saved);
    }

    @Transactional
    public SubmissionResponse grade(Long submissionId, GradeRequest request) {
        Submission submission = submissionRepository.getOrThrow(submissionId);
        Course course = submission.getAssignment().resolveCourse();
        if (!course.getTeacher().getId().equals(request.teacherId())) {
            throw new BusinessRuleException("Оценивать решение может только преподаватель этого курса");
        }
        submission.grade(request.score(), request.feedback());
        log.info("Решение id={} оценено на {} баллов", submissionId, request.score());

        eventPublisher.publishEvent(new SubmissionGradedEvent(submission.getStudent().getId(), submissionId,
                submission.getAssignment().getTitle(), request.score(), submission.getAssignment().getMaxScore()));
        return SubmissionResponse.from(submission);
    }

    public List<SubmissionResponse> findByAssignment(Long assignmentId) {
        assignmentRepository.getOrThrow(assignmentId);
        return submissionRepository.findAllByAssignmentIdOrderBySubmittedAtAsc(assignmentId).stream()
                .map(SubmissionResponse::from)
                .toList();
    }

    public List<SubmissionResponse> findByStudent(Long studentId) {
        userRepository.getOrThrow(studentId);
        return submissionRepository.findAllByStudentIdOrderBySubmittedAtDesc(studentId).stream()
                .map(SubmissionResponse::from)
                .toList();
    }
}
