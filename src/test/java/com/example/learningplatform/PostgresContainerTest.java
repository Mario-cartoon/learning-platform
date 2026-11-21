package com.example.learningplatform;

import com.example.learningplatform.course.dto.CategoryRequest;
import com.example.learningplatform.course.dto.CourseDetailsResponse;
import com.example.learningplatform.course.dto.CourseRequest;
import com.example.learningplatform.course.dto.LessonRequest;
import com.example.learningplatform.course.dto.ModuleRequest;
import com.example.learningplatform.course.service.CategoryService;
import com.example.learningplatform.course.service.CourseService;
import com.example.learningplatform.user.dto.UserRequest;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Те же сущности, но на настоящем PostgreSQL в Docker (Testcontainers).
 * Если Docker не запущен - тест пропускается, в CI на GitHub Actions выполняется.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class PostgresContainerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CourseService courseService;

    @Test
    void schemaIsCreatedInPostgres() {
        List<String> tables = jdbcTemplate.queryForList(
                "select table_name from information_schema.tables where table_schema = 'public'", String.class);

        assertThat(tables).contains("users", "courses", "course_tag", "modules", "lessons",
                "enrollments", "submissions", "quizzes", "quiz_submissions");
    }

    @Test
    void courseWithStructureIsSavedAndReadFromPostgres() {
        Long teacherId = userService.create(new UserRequest("Ирина", "pg-teacher@test.local", Role.TEACHER)).id();
        Long categoryId = categoryService.create(new CategoryRequest("PostgreSQL")).id();

        CourseDetailsResponse created = courseService.create(new CourseRequest("Курс в PostgreSQL", null,
                categoryId, teacherId, 8, null, Set.of("SQL"),
                List.of(new ModuleRequest("Модуль", null, List.of(new LessonRequest("Урок", null, null))))));

        CourseDetailsResponse loaded = courseService.getDetails(created.id());
        assertThat(loaded.tags()).containsExactly("SQL");
        assertThat(loaded.modules()).singleElement()
                .satisfies(module -> assertThat(module.lessons()).hasSize(1));
    }
}
