package com.example.learningplatform.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Название категории обязательно")
        @Size(max = 100, message = "Название не длиннее 100 символов")
        String name) {
}
