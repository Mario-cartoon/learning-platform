package com.example.learningplatform.course.dto;

import com.example.learningplatform.course.entity.Lesson;

public record LessonResponse(Long id, Long moduleId, Integer orderIndex, String title, String content, String videoUrl) {

    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(lesson.getId(), lesson.getModule().getId(), lesson.getOrderIndex(),
                lesson.getTitle(), lesson.getContent(), lesson.getVideoUrl());
    }
}
