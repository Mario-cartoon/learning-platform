package com.example.learningplatform.course;

import com.example.learningplatform.course.dto.CourseDetailsResponse;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.entity.CourseModule;
import com.example.learningplatform.course.repository.CourseModuleRepository;
import com.example.learningplatform.course.repository.CourseRepository;
import com.example.learningplatform.course.service.CourseService;
import com.example.learningplatform.support.AbstractIntegrationTest;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Демонстрация ленивой загрузки: сначала воспроизводим проблему, потом способы её обойти.
 */
class LazyLoadingTest extends AbstractIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseModuleRepository moduleRepository;

    @Autowired
    private CourseService courseService;

    private Long courseId;

    @BeforeEach
    void setUp() {
        courseId = fixtures.courseWithStructure(2, 1).getId();
    }

    @Test
    void lazyCollectionThrowsOutsideTransaction() {
        // транзакция findById уже закрыта, сессии нет
        Course course = courseRepository.findById(courseId).orElseThrow();

        assertThat(Hibernate.isInitialized(course.getModules())).isFalse();
        assertThatThrownBy(() -> course.getModules().size())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void lazyManyToOneIsProxyOutsideTransaction() {
        Course course = courseRepository.findById(courseId).orElseThrow();

        assertThat(Hibernate.isInitialized(course.getTeacher())).isFalse();
        // id у прокси есть и без запроса к базе, а вот остальные поля - нет
        assertThat(course.getTeacher().getId()).isNotNull();
        assertThatThrownBy(() -> course.getTeacher().getName())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void initializingCollectionInsideTransactionHelps() {
        Course course = transactionTemplate.execute(status -> {
            Course loaded = courseRepository.findById(courseId).orElseThrow();
            Hibernate.initialize(loaded.getModules());
            return loaded;
        });

        assertThat(course).isNotNull();
        assertThat(course.getModules()).hasSize(2);
    }

    @Test
    void joinFetchQueryLoadsLessonsTogetherWithModules() {
        List<CourseModule> modules = moduleRepository.findAllWithLessonsByCourseId(courseId);

        assertThat(modules).hasSize(2)
                .allSatisfy(module -> assertThat(Hibernate.isInitialized(module.getLessons())).isTrue());
        assertThat(modules.get(0).getLessons()).hasSize(1);
    }

    @Test
    void entityGraphLoadsTeacherAndCategory() {
        Course course = courseRepository.findWithDetailsById(courseId).orElseThrow();

        assertThat(Hibernate.isInitialized(course.getTeacher())).isTrue();
        assertThat(course.getTeacher().getName()).startsWith("Преподаватель");
        assertThat(course.getCategory().getName()).startsWith("Категория");
    }

    @Test
    void serviceReturnsDtoBuiltInsideTransaction() {
        CourseDetailsResponse details = courseService.getDetails(courseId);

        assertThat(details.modules()).hasSize(2);
        assertThat(details.modules().get(0).lessons()).hasSize(1);
        assertThat(details.teacherName()).isNotBlank();
    }
}
