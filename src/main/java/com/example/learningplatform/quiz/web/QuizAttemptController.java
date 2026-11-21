package com.example.learningplatform.quiz.web;

import com.example.learningplatform.quiz.dto.QuizResultResponse;
import com.example.learningplatform.quiz.dto.TakeQuizRequest;
import com.example.learningplatform.quiz.service.QuizAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService attemptService;

    @PostMapping("/api/quizzes/{quizId}/take")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResultResponse take(@PathVariable Long quizId, @Valid @RequestBody TakeQuizRequest request) {
        return attemptService.takeQuiz(quizId, request);
    }

    @GetMapping("/api/quizzes/{quizId}/results")
    public List<QuizResultResponse> findByQuiz(@PathVariable Long quizId) {
        return attemptService.findResultsByQuiz(quizId);
    }

    @GetMapping("/api/users/{studentId}/quiz-results")
    public List<QuizResultResponse> findByStudent(@PathVariable Long studentId) {
        return attemptService.findResultsByStudent(studentId);
    }
}
