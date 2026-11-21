package com.example.learningplatform.quiz.service;

import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.course.entity.CourseModule;
import com.example.learningplatform.course.repository.CourseModuleRepository;
import com.example.learningplatform.quiz.dto.QuestionRequest;
import com.example.learningplatform.quiz.dto.QuestionResponse;
import com.example.learningplatform.quiz.dto.QuizRequest;
import com.example.learningplatform.quiz.dto.QuizResponse;
import com.example.learningplatform.quiz.entity.Question;
import com.example.learningplatform.quiz.entity.Quiz;
import com.example.learningplatform.quiz.repository.QuestionRepository;
import com.example.learningplatform.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Создание и редактирование тестов (сторона преподавателя).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final CourseModuleRepository moduleRepository;
    private final QuestionFactory questionFactory;

    @Transactional
    public QuizResponse create(Long moduleId, QuizRequest request) {
        CourseModule module = moduleRepository.getOrThrow(moduleId);
        if (quizRepository.existsByModuleId(moduleId)) {
            throw new ConflictException("У модуля уже есть тест");
        }
        Quiz quiz = new Quiz(request.title(), request.timeLimitMinutes(), request.passingScore());
        if (request.questions() != null) {
            request.questions().stream().map(questionFactory::create).forEach(quiz::addQuestion);
        }
        module.attachQuiz(quiz);
        quizRepository.save(quiz);
        log.info("Для модуля id={} создан тест id={}, вопросов: {}", moduleId, quiz.getId(), quiz.getQuestions().size());
        return QuizResponse.from(quiz, quiz.getQuestions());
    }

    public QuizResponse getQuiz(Long quizId) {
        Quiz quiz = quizRepository.getOrThrow(quizId);
        return QuizResponse.from(quiz, questionRepository.findAllWithOptionsByQuizId(quizId));
    }

    @Transactional
    public QuestionResponse addQuestion(Long quizId, QuestionRequest request) {
        Quiz quiz = quizRepository.getOrThrow(quizId);
        Question question = questionFactory.create(request);
        quiz.addQuestion(question);
        questionRepository.save(question);
        return QuestionResponse.from(question);
    }

    @Transactional
    public void delete(Long quizId) {
        Quiz quiz = quizRepository.getOrThrow(quizId);
        quizRepository.delete(quiz);
        log.info("Удалён тест id={}", quizId);
    }
}
