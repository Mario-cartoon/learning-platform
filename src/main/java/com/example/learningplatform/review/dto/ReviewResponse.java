package com.example.learningplatform.review.dto;

import com.example.learningplatform.review.entity.CourseReview;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, Long studentId, String studentName, int rating, String comment,
                             LocalDateTime createdAt) {

    public static ReviewResponse from(CourseReview review) {
        return new ReviewResponse(review.getId(), review.getStudent().getId(), review.getStudent().getName(),
                review.getRating(), review.getComment(), review.getCreatedAt());
    }
}
