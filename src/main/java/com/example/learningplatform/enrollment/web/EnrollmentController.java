package com.example.learningplatform.enrollment.web;

import com.example.learningplatform.enrollment.dto.EnrollmentResponse;
import com.example.learningplatform.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/api/courses/{courseId}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(@PathVariable Long courseId, @RequestParam Long studentId) {
        return enrollmentService.enroll(courseId, studentId);
    }

    @DeleteMapping("/api/courses/{courseId}/enroll")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenroll(@PathVariable Long courseId, @RequestParam Long studentId) {
        enrollmentService.unenroll(courseId, studentId);
    }

    @PatchMapping("/api/courses/{courseId}/enroll/complete")
    public EnrollmentResponse complete(@PathVariable Long courseId, @RequestParam Long studentId) {
        return enrollmentService.complete(courseId, studentId);
    }

    @GetMapping("/api/courses/{courseId}/students")
    public List<EnrollmentResponse> findStudents(@PathVariable Long courseId) {
        return enrollmentService.findStudentsOfCourse(courseId);
    }

    @GetMapping("/api/users/{studentId}/courses")
    public List<EnrollmentResponse> findCourses(@PathVariable Long studentId) {
        return enrollmentService.findCoursesOfStudent(studentId);
    }
}
