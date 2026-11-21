package com.example.learningplatform.quiz.dto;

import com.example.learningplatform.quiz.entity.AnswerOption;

/**
 * Без признака правильности - этот ответ видит студент.
 */
public record OptionResponse(Long id, String text) {

    public static OptionResponse from(AnswerOption option) {
        return new OptionResponse(option.getId(), option.getText());
    }
}
