package com.example.learningplatform.course.dto;

import com.example.learningplatform.course.entity.Lesson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonRequest(
        @NotBlank(message = "Название урока обязательно")
        @Size(max = 200, message = "Название не длиннее 200 символов")
        String title,

        @Size(max = 10000, message = "Текст урока не длиннее 10000 символов")
        String content,

        @Size(max = 500, message = "Ссылка на видео не длиннее 500 символов")
        String videoUrl) {

    public Lesson toEntity() {
        return new Lesson(title, content, videoUrl);
    }
}
