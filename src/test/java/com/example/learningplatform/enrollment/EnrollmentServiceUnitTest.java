package com.example.learningplatform.enrollment;

import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.course.entity.Category;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.repository.CourseRepository;
import com.example.learningplatform.enrollment.dto.EnrollmentResponse;
import com.example.learningplatform.enrollment.entity.Enrollment;
import com.example.learningplatform.enrollment.repository.EnrollmentRepository;
import com.example.learningplatform.enrollment.service.EnrollmentService;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;
import com.example.learningplatform.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тест сервиса на моках: проверяем только логику, без базы.
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceUnitTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void enrollSavesNewEnrollment() {
        User student = withId(new User("Анна", "anna@test.local", Role.STUDENT), 1L);
        Course course = withId(course(), 10L);
        when(userRepository.getOrThrow(1L)).thenReturn(student);
        when(courseRepository.getOrThrow(10L)).thenReturn(course);
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentResponse response = enrollmentService.enroll(10L, 1L);

        assertThat(response.studentId()).isEqualTo(1L);
        assertThat(response.courseId()).isEqualTo(10L);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void duplicateEnrollmentIsNotSaved() {
        User student = withId(new User("Анна", "anna@test.local", Role.STUDENT), 1L);
        when(userRepository.getOrThrow(1L)).thenReturn(student);
        when(courseRepository.getOrThrow(10L)).thenReturn(withId(course(), 10L));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enroll(10L, 1L)).isInstanceOf(ConflictException.class);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void teacherCannotBeEnrolled() {
        User teacher = withId(new User("Ирина", "irina@test.local", Role.TEACHER), 2L);
        when(userRepository.getOrThrow(2L)).thenReturn(teacher);

        assertThatThrownBy(() -> enrollmentService.enroll(10L, 2L)).isInstanceOf(BusinessRuleException.class);
        verify(enrollmentRepository, never()).save(any());
    }

    private static Course course() {
        return new Course("Курс", null, new Category("IT"), new User("Ирина", "irina@test.local", Role.TEACHER));
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
