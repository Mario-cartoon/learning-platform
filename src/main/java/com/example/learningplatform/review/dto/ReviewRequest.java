package com.example.learningplatform.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull(message = "Нужно указать студента")
        Long studentId,

        @NotNull(message = "Нужно поставить оценку")
        @Min(value = 1, message = "Оценка от 1 до 5")
        @Max(value = 5, message = "Оценка от 1 до 5")
        Integer rating,

        @Size(max = 2000, message = "Отзыв не длиннее 2000 символов")
        String comment) {
}
