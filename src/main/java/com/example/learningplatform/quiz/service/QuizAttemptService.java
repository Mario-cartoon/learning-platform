package com.example.learningplatform.quiz.service;

import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.enrollment.service.EnrollmentPolicy;
import com.example.learningplatform.quiz.dto.QuizResultResponse;
import com.example.learningplatform.quiz.dto.TakeQuizRequest;
import com.example.learningplatform.quiz.entity.Question;
import com.example.learningplatform.quiz.entity.Quiz;
import com.example.learningplatform.quiz.entity.QuizSubmission;
import com.example.learningplatform.quiz.grading.QuizScoreCalculator;
import com.example.learningplatform.quiz.repository.QuestionRepository;
import com.example.learningplatform.quiz.repository.QuizRepository;
import com.example.learningplatform.quiz.repository.QuizSubmissionRepository;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;
import com.example.learningplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Прохождение тестов и результаты (сторона студента).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizAttemptService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserRepository userRepository;
    private final EnrollmentPolicy enrollmentPolicy;
    private final QuizScoreCalculator scoreCalculator;

    @Transactional
    public QuizResultResponse takeQuiz(Long quizId, TakeQuizRequest request) {
        Quiz quiz = quizRepository.getOrThrow(quizId);
        User student = userRepository.getOrThrow(request.studentId());
        student.requireRole(Role.STUDENT);
        enrollmentPolicy.ensureEnrolled(student.getId(), quiz.getModule().getCourse().getId());

        List<Question> questions = questionRepository.findAllWithOptionsByQuizId(quizId);
        if (questions.isEmpty()) {
            throw new BusinessRuleException("В тесте пока нет вопросов");
        }
        int score = scoreCalculator.calculatePercent(questions, request.answers());
        QuizSubmission saved = quizSubmissionRepository.save(new QuizSubmission(quiz, student, score));
        log.info("Студент id={} прошёл тест id={}: {}%", student.getId(), quizId, score);
        return QuizResultResponse.from(saved);
    }

    public List<QuizResultResponse> findResultsByQuiz(Long quizId) {
        quizRepository.getOrThrow(quizId);
        return quizSubmissionRepository.findAllByQuizIdOrderByTakenAtDesc(quizId).stream()
                .map(QuizResultResponse::from)
                .toList();
    }

    public List<QuizResultResponse> findResultsByStudent(Long studentId) {
        userRepository.getOrThrow(studentId);
        return quizSubmissionRepository.findAllByStudentIdOrderByTakenAtDesc(studentId).stream()
                .map(QuizResultResponse::from)
                .toList();
    }
}
