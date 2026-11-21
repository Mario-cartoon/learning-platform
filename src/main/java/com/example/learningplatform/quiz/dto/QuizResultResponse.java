package com.example.learningplatform.quiz.dto;

import com.example.learningplatform.quiz.entity.QuizSubmission;

import java.time.LocalDateTime;

public record QuizResultResponse(Long id, Long quizId, String quizTitle, Long studentId, String studentName,
                                 int score, boolean passed, LocalDateTime takenAt) {

    public static QuizResultResponse from(QuizSubmission submission) {
        return new QuizResultResponse(submission.getId(),
                submission.getQuiz().getId(), submission.getQuiz().getTitle(),
                submission.getStudent().getId(), submission.getStudent().getName(),
                submission.getScore(), submission.isPassed(), submission.getTakenAt());
    }
}
