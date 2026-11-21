package com.example.learningplatform.notification.service;

import com.example.learningplatform.assignment.event.AssignmentCreatedEvent;
import com.example.learningplatform.assignment.event.SubmissionGradedEvent;
import com.example.learningplatform.enrollment.repository.EnrollmentRepository;
import com.example.learningplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Слушает события других модулей (паттерн Observer через ApplicationEvent)
 * и создаёт уведомления. Выполняется в той же транзакции, что и исходное действие.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @EventListener
    public void onAssignmentCreated(AssignmentCreatedEvent event) {
        String message = "Новое задание: " + event.assignmentTitle();
        enrollmentRepository.findAllByCourseIdOrderByEnrolledAtAsc(event.courseId())
                .forEach(enrollment -> notificationService.notify(enrollment.getStudent(), message));
    }

    @EventListener
    public void onSubmissionGraded(SubmissionGradedEvent event) {
        String message = String.format("Ваше решение по заданию «%s» проверено: %d из %d",
                event.assignmentTitle(), event.score(), event.maxScore());
        notificationService.notify(userRepository.getReferenceById(event.studentId()), message);
    }
}
