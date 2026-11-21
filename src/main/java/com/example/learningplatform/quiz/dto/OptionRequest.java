package com.example.learningplatform.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OptionRequest(
        @NotBlank(message = "Текст варианта обязателен")
        @Size(max = 500, message = "Вариант не длиннее 500 символов")
        String text,

        boolean correct) {
}
