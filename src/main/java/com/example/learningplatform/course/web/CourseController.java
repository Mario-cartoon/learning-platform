package com.example.learningplatform.course.web;

import com.example.learningplatform.course.dto.CourseDetailsResponse;
import com.example.learningplatform.course.dto.CourseRequest;
import com.example.learningplatform.course.dto.CourseSummaryResponse;
import com.example.learningplatform.course.dto.ModuleRequest;
import com.example.learningplatform.course.dto.ModuleResponse;
import com.example.learningplatform.course.service.CourseContentService;
import com.example.learningplatform.course.service.CourseService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseContentService contentService;

    @GetMapping
    public List<CourseSummaryResponse> findAll(@RequestParam(required = false) Long categoryId,
                                               @RequestParam(required = false) String tag) {
        return courseService.findAll(categoryId, tag);
    }

    @GetMapping("/{id}")
    public CourseDetailsResponse getDetails(@PathVariable Long id) {
        return courseService.getDetails(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDetailsResponse create(@Valid @RequestBody CourseRequest request) {
        return courseService.create(request);
    }

    @PutMapping("/{id}")
    public CourseDetailsResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }

    @PostMapping("/{id}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleResponse addModule(@PathVariable Long id, @Valid @RequestBody ModuleRequest request) {
        return contentService.addModule(id, request);
    }
}
