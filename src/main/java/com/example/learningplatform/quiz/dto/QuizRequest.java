package com.example.learningplatform.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuizRequest(
        @NotBlank(message = "Название теста обязательно")
        @Size(max = 200, message = "Название не длиннее 200 символов")
        String title,

        @Positive(message = "Лимит времени должен быть положительным")
        Integer timeLimitMinutes,

        @NotNull(message = "Нужно указать проходной балл")
        @Min(value = 0, message = "Проходной балл от 0 до 100")
        @Max(value = 100, message = "Проходной балл от 0 до 100")
        Integer passingScore,

        List<@Valid QuestionRequest> questions) {
}
