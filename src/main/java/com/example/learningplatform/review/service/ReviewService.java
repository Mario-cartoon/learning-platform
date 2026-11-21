package com.example.learningplatform.review.service;

import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.repository.CourseRepository;
import com.example.learningplatform.enrollment.service.EnrollmentPolicy;
import com.example.learningplatform.review.dto.CourseReviewsResponse;
import com.example.learningplatform.review.dto.ReviewRequest;
import com.example.learningplatform.review.dto.ReviewResponse;
import com.example.learningplatform.review.entity.CourseReview;
import com.example.learningplatform.review.repository.CourseReviewRepository;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;
import com.example.learningplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentPolicy enrollmentPolicy;

    @Transactional
    public ReviewResponse addReview(Long courseId, ReviewRequest request) {
        Course course = courseRepository.getOrThrow(courseId);
        User student = userRepository.getOrThrow(request.studentId());
        student.requireRole(Role.STUDENT);
        enrollmentPolicy.ensureEnrolled(student.getId(), courseId);
        if (reviewRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new ConflictException("Студент уже оставил отзыв на этот курс");
        }
        CourseReview saved = reviewRepository.save(
                new CourseReview(course, student, request.rating(), request.comment()));
        log.info("Отзыв id={} на курс id={} от студента id={}", saved.getId(), courseId, student.getId());
        return ReviewResponse.from(saved);
    }

    public CourseReviewsResponse findByCourse(Long courseId) {
        courseRepository.getOrThrow(courseId);
        List<ReviewResponse> reviews = reviewRepository.findAllByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(ReviewResponse::from)
                .toList();
        return new CourseReviewsResponse(courseId, reviewRepository.findAverageRating(courseId), reviews.size(), reviews);
    }

    @Transactional
    public void delete(Long reviewId) {
        reviewRepository.delete(reviewRepository.getOrThrow(reviewId));
        log.info("Удалён отзыв id={}", reviewId);
    }
}
