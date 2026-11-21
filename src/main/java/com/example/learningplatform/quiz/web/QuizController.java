package com.example.learningplatform.quiz.web;

import com.example.learningplatform.quiz.dto.QuestionRequest;
import com.example.learningplatform.quiz.dto.QuestionResponse;
import com.example.learningplatform.quiz.dto.QuizRequest;
import com.example.learningplatform.quiz.dto.QuizResponse;
import com.example.learningplatform.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/api/modules/{moduleId}/quiz")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse create(@PathVariable Long moduleId, @Valid @RequestBody QuizRequest request) {
        return quizService.create(moduleId, request);
    }

    @GetMapping("/api/quizzes/{id}")
    public QuizResponse getQuiz(@PathVariable Long id) {
        return quizService.getQuiz(id);
    }

    @PostMapping("/api/quizzes/{id}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse addQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return quizService.addQuestion(id, request);
    }

    @DeleteMapping("/api/quizzes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        quizService.delete(id);
    }
}
