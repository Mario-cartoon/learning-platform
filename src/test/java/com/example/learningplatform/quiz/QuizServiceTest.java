package com.example.learningplatform.quiz;

import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.quiz.dto.OptionRequest;
import com.example.learningplatform.quiz.dto.QuestionRequest;
import com.example.learningplatform.quiz.dto.QuizRequest;
import com.example.learningplatform.quiz.dto.QuizResponse;
import com.example.learningplatform.quiz.dto.QuizResultResponse;
import com.example.learningplatform.quiz.dto.TakeQuizRequest;
import com.example.learningplatform.quiz.entity.QuestionType;
import com.example.learningplatform.quiz.service.QuizAttemptService;
import com.example.learningplatform.quiz.service.QuizService;
import com.example.learningplatform.support.AbstractIntegrationTest;
import com.example.learningplatform.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuizServiceTest extends AbstractIntegrationTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizAttemptService attemptService;

    private Long moduleId;
    private User student;

    @BeforeEach
    void setUp() {
        Course course = fixtures.courseWithStructure(1, 1);
        moduleId = course.getModules().get(0).getId();
        student = fixtures.student();
        fixtures.enroll(student, course);
    }

    @Test
    void createdQuizIsReadWithQuestionsAndOptions() {
        Long quizId = createQuiz().id();

        QuizResponse quiz = quizService.getQuiz(quizId);

        assertThat(quiz.moduleId()).isEqualTo(moduleId);
        assertThat(quiz.questions()).hasSize(2);
        assertThat(quiz.questions().get(0).options()).hasSize(2);
        assertThat(quiz.questions().get(1).options()).hasSize(3);
    }

    @Test
    void allCorrectAnswersGiveHundredPercent() {
        QuizResponse quiz = createQuiz();

        QuizResultResponse result = attemptService.takeQuiz(quiz.id(), new TakeQuizRequest(student.getId(), Map.of(
                questionId(quiz, 0), Set.of(optionId(quiz, 0, "4")),
                questionId(quiz, 1), Set.of(optionId(quiz, 1, "2"), optionId(quiz, 1, "4")))));

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.passed()).isTrue();
        assertThat(attemptService.findResultsByQuiz(quiz.id())).hasSize(1);
        assertThat(attemptService.findResultsByStudent(student.getId())).hasSize(1);
    }

    @Test
    void multipleChoiceIsCountedPartially() {
        QuizResponse quiz = createQuiz();

        // первый вопрос верно (1.0), во втором выбран один из двух верных (0.5) -> 75%
        QuizResultResponse result = attemptService.takeQuiz(quiz.id(), new TakeQuizRequest(student.getId(), Map.of(
                questionId(quiz, 0), Set.of(optionId(quiz, 0, "4")),
                questionId(quiz, 1), Set.of(optionId(quiz, 1, "2")))));

        assertThat(result.score()).isEqualTo(75);
        assertThat(result.passed()).isTrue();
    }

    @Test
    void wrongAnswersDoNotPass() {
        QuizResponse quiz = createQuiz();

        QuizResultResponse result = attemptService.takeQuiz(quiz.id(), new TakeQuizRequest(student.getId(), Map.of(
                questionId(quiz, 0), Set.of(optionId(quiz, 0, "5")),
                questionId(quiz, 1), Set.of(optionId(quiz, 1, "3")))));

        assertThat(result.score()).isZero();
        assertThat(result.passed()).isFalse();
    }

    @Test
    void moduleCanHaveOnlyOneQuiz() {
        createQuiz();

        assertThatThrownBy(this::createQuiz).isInstanceOf(ConflictException.class);
    }

    @Test
    void singleChoiceQuestionWithTwoCorrectOptionsIsRejected() {
        Long quizId = createQuiz().id();
        QuestionRequest invalid = new QuestionRequest("Плохой вопрос", QuestionType.SINGLE_CHOICE, List.of(
                new OptionRequest("A", true), new OptionRequest("B", true)));

        assertThatThrownBy(() -> quizService.addQuestion(quizId, invalid)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void notEnrolledStudentCannotTakeQuiz() {
        QuizResponse quiz = createQuiz();
        User stranger = fixtures.student();

        assertThatThrownBy(() -> attemptService.takeQuiz(quiz.id(), new TakeQuizRequest(stranger.getId(), Map.of())))
                .isInstanceOf(BusinessRuleException.class);
    }

    private QuizResponse createQuiz() {
        return quizService.create(moduleId, new QuizRequest("Тест", 10, 60, List.of(
                new QuestionRequest("2 + 2 = ?", QuestionType.SINGLE_CHOICE, List.of(
                        new OptionRequest("4", true),
                        new OptionRequest("5", false))),
                new QuestionRequest("Какие числа чётные?", QuestionType.MULTIPLE_CHOICE, List.of(
                        new OptionRequest("2", true),
                        new OptionRequest("4", true),
                        new OptionRequest("3", false))))));
    }

    private Long questionId(QuizResponse quiz, int index) {
        return quiz.questions().get(index).id();
    }

    private Long optionId(QuizResponse quiz, int questionIndex, String text) {
        return quiz.questions().get(questionIndex).options().stream()
                .filter(option -> option.text().equals(text))
                .findFirst()
                .orElseThrow()
                .id();
    }
}
