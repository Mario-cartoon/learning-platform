package com.example.learningplatform.support;

import com.example.learningplatform.course.entity.Category;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.entity.CourseModule;
import com.example.learningplatform.course.entity.Lesson;
import com.example.learningplatform.course.repository.CategoryRepository;
import com.example.learningplatform.course.repository.CourseRepository;
import com.example.learningplatform.enrollment.entity.Enrollment;
import com.example.learningplatform.enrollment.repository.EnrollmentRepository;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;
import com.example.learningplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Готовые тестовые данные, чтобы не повторять одно и то же в каждом тесте.
 */
@TestComponent
@RequiredArgsConstructor
public class TestFixtures {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    private final AtomicInteger sequence = new AtomicInteger();

    public User teacher() {
        return user("Преподаватель", Role.TEACHER);
    }

    public User student() {
        return user("Студент", Role.STUDENT);
    }

    public Category category() {
        return categoryRepository.save(new Category("Категория " + sequence.incrementAndGet()));
    }

    public Course course() {
        return courseWithStructure(0, 0);
    }

    /**
     * Курс с модулями "Модуль i" и уроками "Урок i.j", сохраняется одним save() за счёт каскада.
     */
    public Course courseWithStructure(int modules, int lessonsPerModule) {
        Course course = new Course("Курс " + sequence.incrementAndGet(), "Описание курса", category(), teacher());
        for (int i = 1; i <= modules; i++) {
            CourseModule module = new CourseModule("Модуль " + i, null);
            for (int j = 1; j <= lessonsPerModule; j++) {
                module.addLesson(new Lesson("Урок " + i + "." + j, "Текст урока", null));
            }
            course.addModule(module);
        }
        return courseRepository.save(course);
    }

    public Enrollment enroll(User student, Course course) {
        return enrollmentRepository.save(new Enrollment(student, course));
    }

    private User user(String name, Role role) {
        int number = sequence.incrementAndGet();
        String email = role.name().toLowerCase(Locale.ROOT) + number + "@test.local";
        return userRepository.save(new User(name + " " + number, email, role));
    }
}
