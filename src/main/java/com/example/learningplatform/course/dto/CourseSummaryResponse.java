package com.example.learningplatform.course.dto;

import com.example.learningplatform.course.entity.Course;

import java.time.LocalDate;

public record CourseSummaryResponse(Long id, String title, String description, String category,
                                    Long teacherId, String teacherName, Integer durationHours, LocalDate startDate) {

    public static CourseSummaryResponse from(Course course) {
        return new CourseSummaryResponse(course.getId(), course.getTitle(), course.getDescription(),
                course.getCategory().getName(), course.getTeacher().getId(), course.getTeacher().getName(),
                course.getDurationHours(), course.getStartDate());
    }
}
