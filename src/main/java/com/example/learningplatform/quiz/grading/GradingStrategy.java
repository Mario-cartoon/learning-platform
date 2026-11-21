package com.example.learningplatform.quiz.grading;

import com.example.learningplatform.quiz.entity.Question;
import com.example.learningplatform.quiz.entity.QuestionType;

import java.util.Set;

/**
 * Стратегия проверки ответа на вопрос. Для каждого типа вопроса - своя реализация,
 * новый тип добавляется новым классом без правки существующего кода.
 */
public interface GradingStrategy {

    QuestionType supportedType();

    /**
     * @return доля балла за вопрос от 0.0 до 1.0
     */
    double grade(Question question, Set<Long> selectedOptionIds);
}
