package com.example.learningplatform.enrollment.repository;

import com.example.learningplatform.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByCourseId(Long courseId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<Enrollment> findAllByCourseIdOrderByEnrolledAtAsc(Long courseId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<Enrollment> findAllByStudentIdOrderByEnrolledAtDesc(Long studentId);
}
