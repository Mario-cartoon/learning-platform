package com.example.learningplatform.assignment.event;

public record AssignmentCreatedEvent(Long courseId, Long assignmentId, String assignmentTitle) {
}
