package com.example.learningplatform.course.dto;

import com.example.learningplatform.course.entity.CourseModule;

import java.util.List;

public record ModuleResponse(Long id, Integer orderIndex, String title, String description,
                             Long quizId, List<LessonResponse> lessons) {

    public static ModuleResponse from(CourseModule module) {
        Long quizId = module.getQuiz() == null ? null : module.getQuiz().getId();
        List<LessonResponse> lessons = module.getLessons().stream().map(LessonResponse::from).toList();
        return new ModuleResponse(module.getId(), module.getOrderIndex(), module.getTitle(),
                module.getDescription(), quizId, lessons);
    }
}
