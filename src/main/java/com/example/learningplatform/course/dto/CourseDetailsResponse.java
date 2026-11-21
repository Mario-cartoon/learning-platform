package com.example.learningplatform.course.dto;

import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.entity.CourseModule;
import com.example.learningplatform.course.entity.Tag;

import java.time.LocalDate;
import java.util.List;

public record CourseDetailsResponse(Long id, String title, String description, Integer durationHours,
                                    LocalDate startDate, CategoryResponse category, Long teacherId,
                                    String teacherName, List<String> tags, List<ModuleResponse> modules) {

    public static CourseDetailsResponse from(Course course, List<CourseModule> modules) {
        List<String> tags = course.getTags().stream().map(Tag::getName).sorted().toList();
        return new CourseDetailsResponse(course.getId(), course.getTitle(), course.getDescription(),
                course.getDurationHours(), course.getStartDate(), CategoryResponse.from(course.getCategory()),
                course.getTeacher().getId(), course.getTeacher().getName(), tags,
                modules.stream().map(ModuleResponse::from).toList());
    }
}
