package com.example.learningplatform.assignment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GradeRequest(
        @NotNull(message = "Нужно указать преподавателя")
        Long teacherId,

        @NotNull(message = "Нужно указать оценку")
        @Min(value = 0, message = "Оценка не может быть отрицательной")
        Integer score,

        @Size(max = 2000, message = "Комментарий не длиннее 2000 символов")
        String feedback) {
}
