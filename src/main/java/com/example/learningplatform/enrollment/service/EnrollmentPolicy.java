package com.example.learningplatform.enrollment.service;

import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Проверка "студент записан на курс". Нужна заданиям, тестам и отзывам,
 * поэтому вынесена отдельно, чтобы эти модули не зависели от всего EnrollmentService.
 */
@Component
@RequiredArgsConstructor
public class EnrollmentPolicy {

    private final EnrollmentRepository enrollmentRepository;

    public void ensureEnrolled(Long studentId, Long courseId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessRuleException(String.format(
                    "Студент id=%d не записан на курс id=%d", studentId, courseId));
        }
    }
}
