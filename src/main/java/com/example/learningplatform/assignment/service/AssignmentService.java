package com.example.learningplatform.assignment.service;

import com.example.learningplatform.assignment.dto.AssignmentRequest;
import com.example.learningplatform.assignment.dto.AssignmentResponse;
import com.example.learningplatform.assignment.entity.Assignment;
import com.example.learningplatform.assignment.event.AssignmentCreatedEvent;
import com.example.learningplatform.assignment.repository.AssignmentRepository;
import com.example.learningplatform.course.entity.Lesson;
import com.example.learningplatform.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AssignmentResponse create(Long lessonId, AssignmentRequest request) {
        Lesson lesson = lessonRepository.getOrThrow(lessonId);
        Assignment assignment = new Assignment(request.title(), request.description(),
                request.dueDate(), request.maxScore());
        lesson.addAssignment(assignment);
        assignmentRepository.save(assignment);
        log.info("К уроку id={} добавлено задание id={}", lessonId, assignment.getId());
        // уведомления студентам рассылает notification-модуль, сервис про него не знает
        eventPublisher.publishEvent(new AssignmentCreatedEvent(
                assignment.resolveCourse().getId(), assignment.getId(), assignment.getTitle()));
        return AssignmentResponse.from(assignment);
    }

    public List<AssignmentResponse> findByLesson(Long lessonId) {
        lessonRepository.getOrThrow(lessonId);
        return assignmentRepository.findAllByLessonIdOrderByIdAsc(lessonId).stream()
                .map(AssignmentResponse::from)
                .toList();
    }

    public AssignmentResponse findById(Long id) {
        return AssignmentResponse.from(assignmentRepository.getOrThrow(id));
    }

    @Transactional
    public AssignmentResponse update(Long id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.getOrThrow(id);
        assignment.setTitle(request.title());
        assignment.setDescription(request.description());
        assignment.setDueDate(request.dueDate());
        assignment.setMaxScore(request.maxScore());
        return AssignmentResponse.from(assignment);
    }

    @Transactional
    public void delete(Long id) {
        Assignment assignment = assignmentRepository.getOrThrow(id);
        assignment.getLesson().removeAssignment(assignment);
        log.info("Удалено задание id={} вместе с решениями", id);
    }
}
