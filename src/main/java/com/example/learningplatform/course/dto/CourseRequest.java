package com.example.learningplatform.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * modules учитываются только при создании курса (вся структура сохраняется каскадом).
 */
public record CourseRequest(
        @NotBlank(message = "Название курса обязательно")
        @Size(max = 200, message = "Название не длиннее 200 символов")
        String title,

        @Size(max = 4000, message = "Описание не длиннее 4000 символов")
        String description,

        @NotNull(message = "Нужно указать категорию")
        Long categoryId,

        @NotNull(message = "Нужно указать преподавателя")
        Long teacherId,

        @Positive(message = "Длительность должна быть положительной")
        Integer durationHours,

        LocalDate startDate,

        Set<@NotBlank @Size(max = 50) String> tags,

        List<@Valid ModuleRequest> modules) {
}
