package com.example.learningplatform.quiz.repository;

import com.example.learningplatform.quiz.entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
}
