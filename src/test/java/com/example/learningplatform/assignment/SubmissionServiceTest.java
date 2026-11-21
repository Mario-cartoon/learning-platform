package com.example.learningplatform.assignment;

import com.example.learningplatform.assignment.dto.AssignmentRequest;
import com.example.learningplatform.assignment.dto.AssignmentResponse;
import com.example.learningplatform.assignment.dto.GradeRequest;
import com.example.learningplatform.assignment.dto.SubmissionRequest;
import com.example.learningplatform.assignment.dto.SubmissionResponse;
import com.example.learningplatform.assignment.entity.SubmissionStatus;
import com.example.learningplatform.assignment.repository.SubmissionRepository;
import com.example.learningplatform.assignment.service.AssignmentService;
import com.example.learningplatform.assignment.service.SubmissionService;
import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.notification.dto.NotificationResponse;
import com.example.learningplatform.notification.service.NotificationService;
import com.example.learningplatform.support.AbstractIntegrationTest;
import com.example.learningplatform.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmissionServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private NotificationService notificationService;

    private Course course;
    private User student;
    private Long lessonId;

    @BeforeEach
    void setUp() {
        course = fixtures.courseWithStructure(1, 1);
        lessonId = course.getModules().get(0).getLessons().get(0).getId();
        student = fixtures.student();
        fixtures.enroll(student, course);
    }

    @Test
    void studentSubmitsAndTeacherGrades() {
        AssignmentResponse assignment = createAssignment(LocalDateTime.now().plusDays(7));

        SubmissionResponse submitted = submissionService.submit(assignment.id(),
                new SubmissionRequest(student.getId(), "Моё решение"));
        SubmissionResponse graded = submissionService.grade(submitted.id(),
                new GradeRequest(teacherId(), 85, "Хорошо"));

        assertThat(submitted.status()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(graded.status()).isEqualTo(SubmissionStatus.GRADED);
        assertThat(graded.score()).isEqualTo(85);
        assertThat(submissionService.findByAssignment(assignment.id())).hasSize(1);
        assertThat(submissionService.findByStudent(student.getId()))
                .extracting(SubmissionResponse::feedback).containsExactly("Хорошо");
    }

    @Test
    void studentGetsNotificationsAboutNewAssignmentAndGrade() {
        AssignmentResponse assignment = createAssignment(null);
        SubmissionResponse submitted = submissionService.submit(assignment.id(),
                new SubmissionRequest(student.getId(), "Решение"));
        submissionService.grade(submitted.id(), new GradeRequest(teacherId(), 100, null));

        assertThat(notificationService.findByUser(student.getId()))
                .extracting(NotificationResponse::message)
                .hasSize(2)
                .anySatisfy(message -> assertThat(message).startsWith("Новое задание"))
                .anySatisfy(message -> assertThat(message).contains("проверено"));
    }

    @Test
    void secondSubmissionIsRejected() {
        AssignmentResponse assignment = createAssignment(null);
        submissionService.submit(assignment.id(), new SubmissionRequest(student.getId(), "Первое"));

        assertThatThrownBy(() -> submissionService.submit(assignment.id(),
                new SubmissionRequest(student.getId(), "Второе")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void notEnrolledStudentCannotSubmit() {
        AssignmentResponse assignment = createAssignment(null);
        User stranger = fixtures.student();

        assertThatThrownBy(() -> submissionService.submit(assignment.id(),
                new SubmissionRequest(stranger.getId(), "Решение")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void overdueSubmissionIsRejected() {
        AssignmentResponse assignment = createAssignment(LocalDateTime.now().minusDays(1));

        assertThatThrownBy(() -> submissionService.submit(assignment.id(),
                new SubmissionRequest(student.getId(), "Поздно")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Срок сдачи");
    }

    @Test
    void scoreAboveMaximumIsRejected() {
        SubmissionResponse submitted = submitSomething();

        assertThatThrownBy(() -> submissionService.grade(submitted.id(), new GradeRequest(teacherId(), 101, null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void onlyCourseTeacherCanGrade() {
        SubmissionResponse submitted = submitSomething();
        User otherTeacher = fixtures.teacher();

        assertThatThrownBy(() -> submissionService.grade(submitted.id(),
                new GradeRequest(otherTeacher.getId(), 50, null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void deletingAssignmentDeletesSubmissions() {
        SubmissionResponse submitted = submitSomething();

        assignmentService.delete(submitted.assignmentId());

        assertThat(submissionRepository.count()).isZero();
    }

    private AssignmentResponse createAssignment(LocalDateTime dueDate) {
        return assignmentService.create(lessonId, new AssignmentRequest("ДЗ 1", "Сделать CRUD", dueDate, 100));
    }

    private SubmissionResponse submitSomething() {
        AssignmentResponse assignment = createAssignment(null);
        return submissionService.submit(assignment.id(), new SubmissionRequest(student.getId(), "Решение"));
    }

    private Long teacherId() {
        return course.getTeacher().getId();
    }
}
