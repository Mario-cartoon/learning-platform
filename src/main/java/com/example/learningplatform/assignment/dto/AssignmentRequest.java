package com.example.learningplatform.assignment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AssignmentRequest(
        @NotBlank(message = "Название задания обязательно")
        @Size(max = 200, message = "Название не длиннее 200 символов")
        String title,

        @Size(max = 4000, message = "Описание не длиннее 4000 символов")
        String description,

        LocalDateTime dueDate,

        @NotNull(message = "Нужно указать максимальный балл")
        @Min(value = 1, message = "Максимальный балл от 1")
        @Max(value = 1000, message = "Максимальный балл до 1000")
        Integer maxScore) {
}
