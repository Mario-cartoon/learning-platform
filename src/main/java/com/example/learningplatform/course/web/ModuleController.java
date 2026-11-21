package com.example.learningplatform.course.web;

import com.example.learningplatform.course.dto.LessonRequest;
import com.example.learningplatform.course.dto.LessonResponse;
import com.example.learningplatform.course.dto.ModuleRequest;
import com.example.learningplatform.course.dto.ModuleResponse;
import com.example.learningplatform.course.service.CourseContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final CourseContentService contentService;

    @PutMapping("/{id}")
    public ModuleResponse update(@PathVariable Long id, @Valid @RequestBody ModuleRequest request) {
        return contentService.updateModule(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contentService.deleteModule(id);
    }

    @GetMapping("/{id}/lessons")
    public List<LessonResponse> findLessons(@PathVariable Long id) {
        return contentService.findLessons(id);
    }

    @PostMapping("/{id}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponse addLesson(@PathVariable Long id, @Valid @RequestBody LessonRequest request) {
        return contentService.addLesson(id, request);
    }
}
