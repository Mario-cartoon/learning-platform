package com.example.learningplatform.assignment.web;

import com.example.learningplatform.assignment.dto.GradeRequest;
import com.example.learningplatform.assignment.dto.SubmissionRequest;
import com.example.learningplatform.assignment.dto.SubmissionResponse;
import com.example.learningplatform.assignment.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/api/assignments/{assignmentId}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse submit(@PathVariable Long assignmentId, @Valid @RequestBody SubmissionRequest request) {
        return submissionService.submit(assignmentId, request);
    }

    @GetMapping("/api/assignments/{assignmentId}/submissions")
    public List<SubmissionResponse> findByAssignment(@PathVariable Long assignmentId) {
        return submissionService.findByAssignment(assignmentId);
    }

    @PutMapping("/api/submissions/{id}/grade")
    public SubmissionResponse grade(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return submissionService.grade(id, request);
    }

    @GetMapping("/api/users/{studentId}/submissions")
    public List<SubmissionResponse> findByStudent(@PathVariable Long studentId) {
        return submissionService.findByStudent(studentId);
    }
}
