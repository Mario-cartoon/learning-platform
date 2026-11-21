package com.example.learningplatform;

import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.enrollment.entity.Enrollment;
import com.example.learningplatform.enrollment.repository.EnrollmentRepository;
import com.example.learningplatform.support.AbstractIntegrationTest;
import com.example.learningplatform.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchemaCreationTest extends AbstractIntegrationTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    void hibernateCreatesTableForEveryEntityAndJoinTable() {
        List<String> tables = jdbcTemplate.queryForList(
                "select lower(table_name) from information_schema.tables where lower(table_schema) = 'public'",
                String.class);

        assertThat(tables).contains(
                "users", "profiles", "categories", "courses", "tags", "course_tag",
                "modules", "lessons", "assignments", "submissions", "enrollments",
                "quizzes", "questions", "answer_options", "quiz_submissions",
                "course_reviews", "notifications");
    }

    @Test
    void foreignKeysAreCreatedForRelations() {
        Integer foreignKeys = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.table_constraints "
                        + "where lower(table_schema) = 'public' and constraint_type = 'FOREIGN KEY'",
                Integer.class);

        assertThat(foreignKeys).isGreaterThanOrEqualTo(20);
    }

    @Test
    void databaseRejectsDuplicateEnrollment() {
        User student = fixtures.student();
        Course course = fixtures.course();
        enrollmentRepository.save(new Enrollment(student, course));

        assertThatThrownBy(() -> enrollmentRepository.save(new Enrollment(student, course)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
