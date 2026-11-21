package com.example.learningplatform.review.web;

import com.example.learningplatform.review.dto.CourseReviewsResponse;
import com.example.learningplatform.review.dto.ReviewRequest;
import com.example.learningplatform.review.dto.ReviewResponse;
import com.example.learningplatform.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/courses/{courseId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse add(@PathVariable Long courseId, @Valid @RequestBody ReviewRequest request) {
        return reviewService.addReview(courseId, request);
    }

    @GetMapping("/api/courses/{courseId}/reviews")
    public CourseReviewsResponse findByCourse(@PathVariable Long courseId) {
        return reviewService.findByCourse(courseId);
    }

    @DeleteMapping("/api/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }
}
