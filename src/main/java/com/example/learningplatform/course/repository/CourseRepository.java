package com.example.learningplatform.course.repository;

import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.course.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // категория и преподаватель нужны в каждом ответе - забираю одним запросом, без N+1
    @Query("select c from Course c join fetch c.category join fetch c.teacher order by c.id")
    List<Course> findAllWithCategoryAndTeacher();

    @Query("select c from Course c join fetch c.category join fetch c.teacher "
            + "where c.category.id = :categoryId order by c.id")
    List<Course> findAllByCategoryIdWithTeacher(@Param("categoryId") Long categoryId);

    @Query("select distinct c from Course c join fetch c.category join fetch c.teacher join c.tags t "
            + "where lower(t.name) = lower(:tag) order by c.id")
    List<Course> findAllByTagName(@Param("tag") String tag);

    @EntityGraph(attributePaths = {"category", "teacher", "tags"})
    Optional<Course> findWithDetailsById(Long id);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByTeacherId(Long teacherId);

    default Course getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Курс", id));
    }
}
