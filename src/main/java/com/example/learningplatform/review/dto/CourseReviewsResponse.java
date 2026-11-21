package com.example.learningplatform.review.dto;

import java.util.List;

public record CourseReviewsResponse(Long courseId, Double averageRating, int reviewsCount, List<ReviewResponse> reviews) {
}
