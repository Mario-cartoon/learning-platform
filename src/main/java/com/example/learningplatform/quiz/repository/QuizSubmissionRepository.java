package com.example.learningplatform.quiz.repository;

import com.example.learningplatform.quiz.entity.QuizSubmission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    @EntityGraph(attributePaths = {"quiz", "student"})
    List<QuizSubmission> findAllByQuizIdOrderByTakenAtDesc(Long quizId);

    @EntityGraph(attributePaths = {"quiz", "student"})
    List<QuizSubmission> findAllByStudentIdOrderByTakenAtDesc(Long studentId);
}
