package com.example.learningplatform.quiz.service;

import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.quiz.dto.OptionRequest;
import com.example.learningplatform.quiz.dto.QuestionRequest;
import com.example.learningplatform.quiz.entity.AnswerOption;
import com.example.learningplatform.quiz.entity.Question;
import com.example.learningplatform.quiz.entity.QuestionType;
import org.springframework.stereotype.Component;

/**
 * Создаёт вопрос с вариантами и проверяет, что он корректный.
 */
@Component
public class QuestionFactory {

    public Question create(QuestionRequest request) {
        long correctCount = request.options().stream().filter(OptionRequest::correct).count();
        if (correctCount == 0) {
            throw new BusinessRuleException("У вопроса должен быть хотя бы один правильный вариант");
        }
        if (request.type() == QuestionType.SINGLE_CHOICE && correctCount > 1) {
            throw new BusinessRuleException("У вопроса SINGLE_CHOICE должен быть ровно один правильный вариант");
        }
        Question question = new Question(request.text(), request.type());
        request.options().forEach(option -> question.addOption(new AnswerOption(option.text(), option.correct())));
        return question;
    }
}
