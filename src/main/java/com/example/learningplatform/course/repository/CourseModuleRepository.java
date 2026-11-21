package com.example.learningplatform.course.repository;

import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.course.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {

    /**
     * Модули курса сразу с уроками и тестом.
     * Курс + модули + уроки одним запросом нельзя (две List-коллекции -> MultipleBagFetchException),
     * поэтому курс и модули грузятся двумя запросами.
     * distinct не нужен: Hibernate 6 сам убирает дубли корневой сущности при join fetch.
     */
    @Query("select m from CourseModule m left join fetch m.lessons left join fetch m.quiz "
            + "where m.course.id = :courseId order by m.orderIndex")
    List<CourseModule> findAllWithLessonsByCourseId(@Param("courseId") Long courseId);

    default CourseModule getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Модуль", id));
    }
}
