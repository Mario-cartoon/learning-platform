package com.example.learningplatform.web;

import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.support.AbstractIntegrationTest;
import com.example.learningplatform.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EnrollmentControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void enrollListAndUnenroll() throws Exception {
        User student = fixtures.student();
        Course course = fixtures.course();

        mockMvc.perform(post("/api/courses/{id}/enroll", course.getId()).param("studentId", student.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/courses/{id}/students", course.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(student.getId()));

        mockMvc.perform(get("/api/users/{id}/courses", student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseId").value(course.getId()));

        mockMvc.perform(delete("/api/courses/{id}/enroll", course.getId()).param("studentId", student.getId().toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void duplicateEnrollmentReturns409() throws Exception {
        User student = fixtures.student();
        Course course = fixtures.course();
        fixtures.enroll(student, course);

        mockMvc.perform(post("/api/courses/{id}/enroll", course.getId()).param("studentId", student.getId().toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Студент уже записан на этот курс"));
    }

    @Test
    void missingParameterReturns400() throws Exception {
        Course course = fixtures.course();

        mockMvc.perform(post("/api/courses/{id}/enroll", course.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownUrlReturns404() throws Exception {
        mockMvc.perform(get("/api/unknown"))
                .andExpect(status().isNotFound());
    }
}
