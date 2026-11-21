package com.example.learningplatform.quiz.grading;

import com.example.learningplatform.quiz.entity.AnswerOption;
import com.example.learningplatform.quiz.entity.Question;
import com.example.learningplatform.quiz.entity.QuestionType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Обычные юнит-тесты без Spring: стратегии оценивания и подсчёт итогового балла.
 */
class GradingStrategiesTest {

    private final SingleChoiceGradingStrategy single = new SingleChoiceGradingStrategy();
    private final MultipleChoiceGradingStrategy multiple = new MultipleChoiceGradingStrategy();

    @Test
    void singleChoiceRightAnswer() {
        Question question = question(0L, QuestionType.SINGLE_CHOICE, true, false, false);

        assertThat(single.grade(question, Set.of(1L))).isEqualTo(1.0);
    }

    @Test
    void singleChoiceWrongOrSeveralOrNoAnswersGiveZero() {
        Question question = question(0L, QuestionType.SINGLE_CHOICE, true, false, false);

        assertThat(single.grade(question, Set.of(2L))).isZero();
        assertThat(single.grade(question, Set.of(1L, 2L))).isZero();
        assertThat(single.grade(question, Set.of())).isZero();
    }

    @Test
    void multipleChoiceAllCorrect() {
        Question question = question(0L, QuestionType.MULTIPLE_CHOICE, true, true, false, false);

        assertThat(multiple.grade(question, Set.of(1L, 2L))).isEqualTo(1.0);
    }

    @Test
    void multipleChoicePartialAnswer() {
        Question question = question(0L, QuestionType.MULTIPLE_CHOICE, true, true, false, false);

        assertThat(multiple.grade(question, Set.of(1L))).isEqualTo(0.5);
        assertThat(multiple.grade(question, Set.of(1L, 2L, 3L))).isEqualTo(0.5);
    }

    @Test
    void multipleChoiceSelectingEverythingGivesZero() {
        Question question = question(0L, QuestionType.MULTIPLE_CHOICE, true, true, false, false);

        assertThat(multiple.grade(question, Set.of(1L, 2L, 3L, 4L))).isZero();
        assertThat(multiple.grade(question, Set.of(1L, 3L))).isZero();
    }

    @Test
    void resolverChoosesStrategyByQuestionType() {
        GradingStrategyResolver resolver = new GradingStrategyResolver(List.of(single, multiple));

        assertThat(resolver.forType(QuestionType.SINGLE_CHOICE)).isSameAs(single);
        assertThat(resolver.forType(QuestionType.MULTIPLE_CHOICE)).isSameAs(multiple);
    }

    @Test
    void resolverFailsForUnknownType() {
        GradingStrategyResolver resolver = new GradingStrategyResolver(List.of(single));

        assertThatThrownBy(() -> resolver.forType(QuestionType.MULTIPLE_CHOICE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void calculatorCountsPercentOverAllQuestions() {
        QuizScoreCalculator calculator = new QuizScoreCalculator(new GradingStrategyResolver(List.of(single, multiple)));
        Question first = question(10L, QuestionType.SINGLE_CHOICE, true, false);
        Question second = question(20L, QuestionType.MULTIPLE_CHOICE, true, true, false);
        Question third = question(30L, QuestionType.SINGLE_CHOICE, false, true);

        // 1.0 + 0.5 + 0 (на третий вопрос нет ответа) = 1.5 из 3 -> 50%
        int percent = calculator.calculatePercent(List.of(first, second, third),
                Map.of(10L, Set.of(11L), 20L, Set.of(21L)));

        assertThat(percent).isEqualTo(50);
    }

    /**
     * Вопрос с вариантами: id вариантов = id вопроса + 1, + 2 ... (для id=0 варианты 1, 2, 3)
     */
    private static Question question(Long id, QuestionType type, boolean... correctFlags) {
        Question question = new Question("Вопрос " + id, type);
        ReflectionTestUtils.setField(question, "id", id);
        for (int i = 0; i < correctFlags.length; i++) {
            AnswerOption option = new AnswerOption("Вариант " + (i + 1), correctFlags[i]);
            ReflectionTestUtils.setField(option, "id", id + i + 1);
            question.addOption(option);
        }
        return question;
    }
}
