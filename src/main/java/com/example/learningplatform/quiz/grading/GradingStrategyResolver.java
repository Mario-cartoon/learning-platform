package com.example.learningplatform.quiz.grading;

import com.example.learningplatform.quiz.entity.QuestionType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Выбирает стратегию по типу вопроса. Spring сам передаёт сюда все реализации GradingStrategy.
 */
@Component
public class GradingStrategyResolver {

    private final Map<QuestionType, GradingStrategy> strategies = new EnumMap<>(QuestionType.class);

    public GradingStrategyResolver(List<GradingStrategy> strategyList) {
        strategyList.forEach(strategy -> strategies.put(strategy.supportedType(), strategy));
    }

    public GradingStrategy forType(QuestionType type) {
        GradingStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalStateException("Нет стратегии проверки для типа вопроса " + type);
        }
        return strategy;
    }
}
