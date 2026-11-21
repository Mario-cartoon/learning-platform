package com.example.learningplatform.user;

import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.enrollment.repository.EnrollmentRepository;
import com.example.learningplatform.support.AbstractIntegrationTest;
import com.example.learningplatform.user.dto.ProfileRequest;
import com.example.learningplatform.user.dto.UserRequest;
import com.example.learningplatform.user.dto.UserResponse;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;
import com.example.learningplatform.user.repository.ProfileRepository;
import com.example.learningplatform.user.repository.UserRepository;
import com.example.learningplatform.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    void createsUserAndOneToOneProfile() {
        UserResponse user = userService.create(new UserRequest("Анна", "Anna@Mail.ru", Role.STUDENT));

        userService.updateProfile(user.id(), new ProfileRequest("Учусь на Java-разработчика", null, "+7 900 123-45-67"));

        assertThat(user.email()).isEqualTo("anna@mail.ru");
        assertThat(userService.getProfile(user.id()).bio()).isEqualTo("Учусь на Java-разработчика");
        assertThat(profileRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        userService.create(new UserRequest("Анна", "anna@mail.ru", Role.STUDENT));

        assertThatThrownBy(() -> userService.create(new UserRequest("Другая Анна", "ANNA@mail.ru", Role.STUDENT)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updatesUser() {
        Long id = userService.create(new UserRequest("Анна", "anna@mail.ru", Role.STUDENT)).id();

        userService.update(id, new UserRequest("Анна Иванова", "anna.ivanova@mail.ru", Role.STUDENT));

        assertThat(userService.findById(id).name()).isEqualTo("Анна Иванова");
        assertThat(userService.findById(id).email()).isEqualTo("anna.ivanova@mail.ru");
    }

    @Test
    void teacherWithCoursesCannotBeDeleted() {
        Course course = fixtures.course();
        Long teacherId = course.getTeacher().getId();

        assertThatThrownBy(() -> userService.delete(teacherId)).isInstanceOf(ConflictException.class);
    }

    @Test
    void deletingStudentRemovesProfileAndEnrollmentsByCascade() {
        User student = fixtures.student();
        fixtures.enroll(student, fixtures.course());
        userService.updateProfile(student.getId(), new ProfileRequest("bio", null, null));

        userService.delete(student.getId());

        assertThat(userRepository.existsById(student.getId())).isFalse();
        assertThat(enrollmentRepository.count()).isZero();
        assertThat(profileRepository.count()).isZero();
        assertThatThrownBy(() -> userService.findById(student.getId())).isInstanceOf(NotFoundException.class);
    }
}
