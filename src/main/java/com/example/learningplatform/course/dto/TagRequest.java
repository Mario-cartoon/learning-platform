package com.example.learningplatform.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank(message = "Название тега обязательно")
        @Size(max = 50, message = "Тег не длиннее 50 символов")
        String name) {
}
