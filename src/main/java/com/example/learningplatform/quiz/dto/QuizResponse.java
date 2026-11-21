package com.example.learningplatform.quiz.dto;

import com.example.learningplatform.quiz.entity.Question;
import com.example.learningplatform.quiz.entity.Quiz;

import java.util.List;

public record QuizResponse(Long id, Long moduleId, String title, Integer timeLimitMinutes, int passingScore,
                           List<QuestionResponse> questions) {

    public static QuizResponse from(Quiz quiz, List<Question> questions) {
        return new QuizResponse(quiz.getId(), quiz.getModule().getId(), quiz.getTitle(),
                quiz.getTimeLimitMinutes(), quiz.getPassingScore(),
                questions.stream().map(QuestionResponse::from).toList());
    }
}
