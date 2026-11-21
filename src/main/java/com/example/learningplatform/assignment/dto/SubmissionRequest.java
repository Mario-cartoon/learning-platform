package com.example.learningplatform.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmissionRequest(
        @NotNull(message = "Нужно указать студента")
        Long studentId,

        @NotBlank(message = "Решение не может быть пустым")
        @Size(max = 10000, message = "Решение не длиннее 10000 символов")
        String content) {
}
