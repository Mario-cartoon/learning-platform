package com.example.learningplatform.quiz.grading;

import com.example.learningplatform.quiz.entity.Question;
import com.example.learningplatform.quiz.entity.QuestionType;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Один правильный вариант: либо угадал, либо нет.
 */
@Component
public class SingleChoiceGradingStrategy implements GradingStrategy {

    @Override
    public QuestionType supportedType() {
        return QuestionType.SINGLE_CHOICE;
    }

    @Override
    public double grade(Question question, Set<Long> selectedOptionIds) {
        if (selectedOptionIds.size() != 1) {
            return 0.0;
        }
        return question.correctOptionIds().containsAll(selectedOptionIds) ? 1.0 : 0.0;
    }
}
