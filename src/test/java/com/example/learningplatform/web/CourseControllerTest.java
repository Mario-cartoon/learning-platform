package com.example.learningplatform.web;

import com.example.learningplatform.course.dto.CourseRequest;
import com.example.learningplatform.course.dto.LessonRequest;
import com.example.learningplatform.course.dto.ModuleRequest;
import com.example.learningplatform.course.entity.Category;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.support.AbstractIntegrationTest;
import com.example.learningplatform.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsCourseWithModulesAndLessons() throws Exception {
        User teacher = fixtures.teacher();
        Category category = fixtures.category();
        CourseRequest request = new CourseRequest("REST-курс", "Описание", category.getId(), teacher.getId(),
                10, null, Set.of("Java"),
                List.of(new ModuleRequest("Модуль 1", null, List.of(new LessonRequest("Урок 1", "Текст", null)))));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.teacherId").value(teacher.getId()))
                .andExpect(jsonPath("$.tags[0]").value("Java"))
                .andExpect(jsonPath("$.modules[0].orderIndex").value(1))
                .andExpect(jsonPath("$.modules[0].lessons[0].title").value("Урок 1"));
    }

    @Test
    void returnsCourseStructureEvenWithOpenInViewDisabled() throws Exception {
        Course course = fixtures.courseWithStructure(2, 2);

        mockMvc.perform(get("/api/courses/{id}", course.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(course.getTitle()))
                .andExpect(jsonPath("$.modules.length()").value(2))
                .andExpect(jsonPath("$.modules[1].lessons.length()").value(2));
    }

    @Test
    void returns404ForUnknownCourse() throws Exception {
        mockMvc.perform(get("/api/courses/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Курс с id=999999 не существует"));
    }

    @Test
    void returns400WithFieldErrorsForInvalidBody() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\", \"durationHours\": -5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.categoryId").exists())
                .andExpect(jsonPath("$.fieldErrors.teacherId").exists())
                .andExpect(jsonPath("$.fieldErrors.durationHours").exists());
    }

    @Test
    void returns400ForBrokenJson() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not a json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns422WhenStudentIsSetAsTeacher() throws Exception {
        User student = fixtures.student();
        Category category = fixtures.category();
        CourseRequest request = new CourseRequest("Курс", null, category.getId(), student.getId(),
                null, null, null, null);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deletesCourse() throws Exception {
        Course course = fixtures.courseWithStructure(1, 1);

        mockMvc.perform(delete("/api/courses/{id}", course.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/courses/{id}", course.getId()))
                .andExpect(status().isNotFound());
    }
}
