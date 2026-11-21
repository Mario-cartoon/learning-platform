package com.example.learningplatform.quiz.grading;

import com.example.learningplatform.quiz.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class QuizScoreCalculator {

    private final GradingStrategyResolver strategyResolver;

    /**
     * @param answers id вопроса -> id выбранных вариантов
     * @return процент правильных ответов 0..100
     */
    public int calculatePercent(List<Question> questions, Map<Long, Set<Long>> answers) {
        if (questions.isEmpty()) {
            return 0;
        }
        double earned = 0;
        for (Question question : questions) {
            Set<Long> selected = answers.get(question.getId());
            earned += strategyResolver.forType(question.getType())
                    .grade(question, selected == null ? Set.of() : selected);
        }
        return (int) Math.round(earned * 100 / questions.size());
    }
}
