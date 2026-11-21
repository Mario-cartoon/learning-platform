package com.example.learningplatform.quiz.dto;

import com.example.learningplatform.quiz.entity.Question;
import com.example.learningplatform.quiz.entity.QuestionType;

import java.util.List;

public record QuestionResponse(Long id, String text, QuestionType type, List<OptionResponse> options) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(question.getId(), question.getText(), question.getType(),
                question.getOptions().stream().map(OptionResponse::from).toList());
    }
}
