package com.example.learningplatform.assignment.event;

public record SubmissionGradedEvent(Long studentId, Long submissionId, String assignmentTitle, int score, int maxScore) {
}
