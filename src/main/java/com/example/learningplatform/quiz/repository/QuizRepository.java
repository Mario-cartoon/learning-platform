package com.example.learningplatform.quiz.repository;

import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    boolean existsByModuleId(Long moduleId);

    default Quiz getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Тест", id));
    }
}
