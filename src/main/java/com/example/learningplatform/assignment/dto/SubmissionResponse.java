package com.example.learningplatform.assignment.dto;

import com.example.learningplatform.assignment.entity.Submission;
import com.example.learningplatform.assignment.entity.SubmissionStatus;

import java.time.LocalDateTime;

public record SubmissionResponse(Long id, Long assignmentId, String assignmentTitle, Long studentId, String studentName,
                                 String content, LocalDateTime submittedAt, SubmissionStatus status,
                                 Integer score, int maxScore, String feedback, LocalDateTime gradedAt) {

    public static SubmissionResponse from(Submission submission) {
        return new SubmissionResponse(submission.getId(),
                submission.getAssignment().getId(), submission.getAssignment().getTitle(),
                submission.getStudent().getId(), submission.getStudent().getName(),
                submission.getContent(), submission.getSubmittedAt(), submission.getStatus(),
                submission.getScore(), submission.getAssignment().getMaxScore(),
                submission.getFeedback(), submission.getGradedAt());
    }
}
