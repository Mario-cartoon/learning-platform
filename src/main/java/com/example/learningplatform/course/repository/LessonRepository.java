package com.example.learningplatform.course.repository;

import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.course.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findAllByModuleIdOrderByOrderIndexAsc(Long moduleId);

    default Lesson getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Урок", id));
    }
}
