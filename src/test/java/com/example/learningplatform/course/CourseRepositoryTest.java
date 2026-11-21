package com.example.learningplatform.course;

import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.entity.CourseModule;
import com.example.learningplatform.course.entity.Lesson;
import com.example.learningplatform.course.entity.Tag;
import com.example.learningplatform.course.repository.CourseModuleRepository;
import com.example.learningplatform.course.repository.CourseRepository;
import com.example.learningplatform.course.repository.LessonRepository;
import com.example.learningplatform.course.repository.TagRepository;
import com.example.learningplatform.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CRUD через репозитории + каскады.
 */
class CourseRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void createSavesModulesAndLessonsByCascade() {
        Course course = fixtures.courseWithStructure(2, 3);

        assertThat(course.getId()).isNotNull();
        assertThat(moduleRepository.count()).isEqualTo(2);
        assertThat(lessonRepository.count()).isEqualTo(6);
    }

    @Test
    void readReturnsStructureInRightOrder() {
        Course course = fixtures.courseWithStructure(2, 2);

        List<CourseModule> modules = moduleRepository.findAllWithLessonsByCourseId(course.getId());

        assertThat(modules).extracting(CourseModule::getOrderIndex).containsExactly(1, 2);
        assertThat(modules.get(0).getLessons()).extracting(Lesson::getTitle).containsExactly("Урок 1.1", "Урок 1.2");
    }

    @Test
    void updateChangesCourseFields() {
        Course course = fixtures.course();

        course.setDescription("Новое описание");
        courseRepository.save(course);

        Course reloaded = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(reloaded.getDescription()).isEqualTo("Новое описание");
    }

    @Test
    void deleteRemovesWholeStructureByCascade() {
        Course course = fixtures.courseWithStructure(2, 2);

        courseRepository.deleteById(course.getId());

        assertThat(courseRepository.existsById(course.getId())).isFalse();
        assertThat(moduleRepository.count()).isZero();
        assertThat(lessonRepository.count()).isZero();
    }

    @Test
    void tagsAreStoredAsManyToMany() {
        Long courseId = fixtures.course().getId();

        transactionTemplate.executeWithoutResult(status -> {
            Course course = courseRepository.findById(courseId).orElseThrow();
            course.addTag(new Tag("Java"));
            course.addTag(new Tag("Hibernate"));
        });

        assertThat(tagRepository.count()).isEqualTo(2);
        assertThat(courseRepository.findAllByTagName("java")).extracting(Course::getId).containsExactly(courseId);
    }

    @Test
    void deletingCourseKeepsTagsForOtherCourses() {
        Long first = fixtures.course().getId();
        Long second = fixtures.course().getId();
        transactionTemplate.executeWithoutResult(status -> {
            Tag java = new Tag("Java");
            courseRepository.findById(first).orElseThrow().addTag(java);
            courseRepository.findById(second).orElseThrow().addTag(java);
        });

        courseRepository.deleteById(first);

        assertThat(tagRepository.count()).isEqualTo(1);
        assertThat(courseRepository.findAllByTagName("Java")).extracting(Course::getId).containsExactly(second);
    }
}
