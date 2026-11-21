package com.example.learningplatform.assignment.repository;

import com.example.learningplatform.assignment.entity.Submission;
import com.example.learningplatform.common.exception.NotFoundException;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    @EntityGraph(attributePaths = {"student", "assignment"})
    List<Submission> findAllByAssignmentIdOrderBySubmittedAtAsc(Long assignmentId);

    @EntityGraph(attributePaths = {"student", "assignment"})
    List<Submission> findAllByStudentIdOrderBySubmittedAtDesc(Long studentId);

    default Submission getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Решение", id));
    }
}
