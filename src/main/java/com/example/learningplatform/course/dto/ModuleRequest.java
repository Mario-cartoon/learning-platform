package com.example.learningplatform.course.dto;

import com.example.learningplatform.course.entity.CourseModule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * lessons можно не передавать - уроки добавляются потом отдельным запросом.
 */
public record ModuleRequest(
        @NotBlank(message = "Название модуля обязательно")
        @Size(max = 200, message = "Название не длиннее 200 символов")
        String title,

        @Size(max = 2000, message = "Описание не длиннее 2000 символов")
        String description,

        List<@Valid LessonRequest> lessons) {

    public CourseModule toEntity() {
        CourseModule module = new CourseModule(title, description);
        if (lessons != null) {
            lessons.forEach(lesson -> module.addLesson(lesson.toEntity()));
        }
        return module;
    }
}
