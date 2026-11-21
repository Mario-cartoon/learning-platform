package com.example.learningplatform.quiz.grading;

import com.example.learningplatform.quiz.entity.Question;
import com.example.learningplatform.quiz.entity.QuestionType;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Несколько правильных вариантов, засчитывается частично:
 * (верно выбранные - ошибочно выбранные) / всего верных, но не меньше 0.
 * Так нельзя набрать балл, просто отметив все варианты.
 */
@Component
public class MultipleChoiceGradingStrategy implements GradingStrategy {

    @Override
    public QuestionType supportedType() {
        return QuestionType.MULTIPLE_CHOICE;
    }

    @Override
    public double grade(Question question, Set<Long> selectedOptionIds) {
        Set<Long> correct = question.correctOptionIds();
        if (correct.isEmpty()) {
            return 0.0;
        }
        long hits = selectedOptionIds.stream().filter(correct::contains).count();
        long mistakes = selectedOptionIds.size() - hits;
        double result = (double) (hits - mistakes) / correct.size();
        return Math.max(0.0, result);
    }
}
