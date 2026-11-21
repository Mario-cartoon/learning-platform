package com.example.learningplatform.quiz.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.Set;

/**
 * answers: {"id вопроса": [id выбранных вариантов]}
 */
public record TakeQuizRequest(
        @NotNull(message = "Нужно указать студента")
        Long studentId,

        @NotNull(message = "Нужны ответы")
        Map<Long, Set<Long>> answers) {
}
