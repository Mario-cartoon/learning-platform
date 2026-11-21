package com.example.learningplatform.assignment.web;

import com.example.learningplatform.assignment.dto.AssignmentRequest;
import com.example.learningplatform.assignment.dto.AssignmentResponse;
import com.example.learningplatform.assignment.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/api/lessons/{lessonId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse create(@PathVariable Long lessonId, @Valid @RequestBody AssignmentRequest request) {
        return assignmentService.create(lessonId, request);
    }

    @GetMapping("/api/lessons/{lessonId}/assignments")
    public List<AssignmentResponse> findByLesson(@PathVariable Long lessonId) {
        return assignmentService.findByLesson(lessonId);
    }

    @GetMapping("/api/assignments/{id}")
    public AssignmentResponse findById(@PathVariable Long id) {
        return assignmentService.findById(id);
    }

    @PutMapping("/api/assignments/{id}")
    public AssignmentResponse update(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
        return assignmentService.update(id, request);
    }

    @DeleteMapping("/api/assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assignmentService.delete(id);
    }
}
