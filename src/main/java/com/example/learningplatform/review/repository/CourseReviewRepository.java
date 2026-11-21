package com.example.learningplatform.review.repository;

import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.review.entity.CourseReview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @EntityGraph(attributePaths = "student")
    List<CourseReview> findAllByCourseIdOrderByCreatedAtDesc(Long courseId);

    @Query("select avg(r.rating) from CourseReview r where r.course.id = :courseId")
    Double findAverageRating(@Param("courseId") Long courseId);

    default CourseReview getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Отзыв", id));
    }
}
