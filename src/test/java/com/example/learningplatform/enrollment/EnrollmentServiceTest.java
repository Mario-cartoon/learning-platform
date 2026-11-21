package com.example.learningplatform.enrollment;

import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.service.CourseService;
import com.example.learningplatform.enrollment.dto.EnrollmentResponse;
import com.example.learningplatform.enrollment.entity.EnrollmentStatus;
import com.example.learningplatform.enrollment.service.EnrollmentService;
import com.example.learningplatform.support.AbstractIntegrationTest;
import com.example.learningplatform.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnrollmentServiceTest extends AbstractIntegrationTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseService courseService;

    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = fixtures.student();
        course = fixtures.course();
    }

    @Test
    void enrollsStudentAndShowsBothSidesOfManyToMany() {
        EnrollmentResponse enrollment = enrollmentService.enroll(course.getId(), student.getId());

        assertThat(enrollment.status()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollmentService.findStudentsOfCourse(course.getId()))
                .extracting(EnrollmentResponse::studentId).containsExactly(student.getId());
        assertThat(enrollmentService.findCoursesOfStudent(student.getId()))
                .extracting(EnrollmentResponse::courseTitle).containsExactly(course.getTitle());
    }

    @Test
    void secondEnrollmentToSameCourseIsRejected() {
        enrollmentService.enroll(course.getId(), student.getId());

        assertThatThrownBy(() -> enrollmentService.enroll(course.getId(), student.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void teacherCannotEnrollAsStudent() {
        User teacher = fixtures.teacher();

        assertThatThrownBy(() -> enrollmentService.enroll(course.getId(), teacher.getId()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void unenrollRemovesRecord() {
        enrollmentService.enroll(course.getId(), student.getId());

        enrollmentService.unenroll(course.getId(), student.getId());

        assertThat(enrollmentService.findStudentsOfCourse(course.getId())).isEmpty();
        assertThatThrownBy(() -> enrollmentService.unenroll(course.getId(), student.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void completeChangesStatusOnlyOnce() {
        enrollmentService.enroll(course.getId(), student.getId());

        EnrollmentResponse completed = enrollmentService.complete(course.getId(), student.getId());

        assertThat(completed.status()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(completed.completedAt()).isNotNull();
        assertThatThrownBy(() -> enrollmentService.complete(course.getId(), student.getId()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void courseWithStudentsCannotBeDeleted() {
        enrollmentService.enroll(course.getId(), student.getId());

        assertThatThrownBy(() -> courseService.delete(course.getId())).isInstanceOf(ConflictException.class);
    }
}
