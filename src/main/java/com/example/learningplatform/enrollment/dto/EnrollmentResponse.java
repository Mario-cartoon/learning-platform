package com.example.learningplatform.enrollment.dto;

import com.example.learningplatform.enrollment.entity.Enrollment;
import com.example.learningplatform.enrollment.entity.EnrollmentStatus;

import java.time.LocalDateTime;

public record EnrollmentResponse(Long id, Long studentId, String studentName, Long courseId, String courseTitle,
                                 EnrollmentStatus status, LocalDateTime enrolledAt, LocalDateTime completedAt) {

    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(enrollment.getId(),
                enrollment.getStudent().getId(), enrollment.getStudent().getName(),
                enrollment.getCourse().getId(), enrollment.getCourse().getTitle(),
                enrollment.getStatus(), enrollment.getEnrolledAt(), enrollment.getCompletedAt());
    }
}
