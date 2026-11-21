package com.example.learningplatform.quiz.dto;

import com.example.learningplatform.quiz.entity.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionRequest(
        @NotBlank(message = "Текст вопроса обязателен")
        @Size(max = 1000, message = "Вопрос не длиннее 1000 символов")
        String text,

        @NotNull(message = "Тип вопроса обязателен: SINGLE_CHOICE или MULTIPLE_CHOICE")
        QuestionType type,

        @NotNull(message = "Нужны варианты ответа")
        @Size(min = 2, max = 10, message = "Вариантов ответа должно быть от 2 до 10")
        List<@Valid OptionRequest> options) {
}
