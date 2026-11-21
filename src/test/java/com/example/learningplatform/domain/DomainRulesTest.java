package com.example.learningplatform.domain;

import com.example.learningplatform.assignment.entity.Assignment;
import com.example.learningplatform.assignment.entity.Submission;
import com.example.learningplatform.assignment.entity.SubmissionStatus;
import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.course.entity.Category;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.entity.CourseModule;
import com.example.learningplatform.course.entity.Tag;
import com.example.learningplatform.enrollment.entity.Enrollment;
import com.example.learningplatform.enrollment.entity.EnrollmentStatus;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Юнит-тесты бизнес-правил, которые живут прямо в сущностях.
 */
class DomainRulesTest {

    private final User teacher = new User("Ирина", "irina@test.local", Role.TEACHER);
    private final User student = new User("Анна", "anna@test.local", Role.STUDENT);

    @Test
    void emailIsNormalized() {
        User user = new User("Олег", "  Oleg@Mail.RU ", Role.STUDENT);

        assertThat(user.getEmail()).isEqualTo("oleg@mail.ru");
    }

    @Test
    void requireRoleThrowsForWrongRole() {
        assertThatThrownBy(() -> student.requireRole(Role.TEACHER)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void modulesAreRenumberedAfterRemoval() {
        Course course = new Course("Курс", null, new Category("IT"), teacher);
        CourseModule first = new CourseModule("Первый", null);
        CourseModule second = new CourseModule("Второй", null);
        CourseModule third = new CourseModule("Третий", null);
        course.addModule(first);
        course.addModule(second);
        course.addModule(third);

        course.removeModule(second);

        assertThat(course.getModules()).containsExactly(first, third);
        assertThat(third.getOrderIndex()).isEqualTo(2);
        assertThat(third.getCourse()).isSameAs(course);
    }

    @Test
    void tagsAreSyncedOnBothSides() {
        Course course = new Course("Курс", null, new Category("IT"), teacher);
        Tag java = new Tag("Java");

        course.addTag(java);
        assertThat(java.getCourses()).containsExactly(course);

        course.replaceTags(Set.of());
        assertThat(course.getTags()).isEmpty();
        assertThat(java.getCourses()).isEmpty();
    }

    @Test
    void gradeSetsScoreAndStatus() {
        Submission submission = new Submission(new Assignment("ДЗ", null, null, 10), student, "Решение");

        submission.grade(8, "Неплохо");

        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.GRADED);
        assertThat(submission.getScore()).isEqualTo(8);
        assertThat(submission.getGradedAt()).isNotNull();
    }

    @Test
    void gradeOutOfRangeIsRejected() {
        Submission submission = new Submission(new Assignment("ДЗ", null, null, 10), student, "Решение");

        assertThatThrownBy(() -> submission.grade(11, null)).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> submission.grade(-1, null)).isInstanceOf(BusinessRuleException.class);
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
    }

    @Test
    void assignmentWithoutDeadlineIsNeverOverdue() {
        LocalDateTime now = LocalDateTime.now();

        assertThat(new Assignment("ДЗ", null, null, 10).isOverdue(now)).isFalse();
        assertThat(new Assignment("ДЗ", null, now.minusHours(1), 10).isOverdue(now)).isTrue();
        assertThat(new Assignment("ДЗ", null, now.plusHours(1), 10).isOverdue(now)).isFalse();
    }

    @Test
    void enrollmentCanBeCompletedOnce() {
        Enrollment enrollment = new Enrollment(student, new Course("Курс", null, new Category("IT"), teacher));

        enrollment.complete();

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThatThrownBy(enrollment::complete).isInstanceOf(BusinessRuleException.class);
    }
}
