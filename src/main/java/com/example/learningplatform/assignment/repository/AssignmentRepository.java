package com.example.learningplatform.assignment.repository;

import com.example.learningplatform.assignment.entity.Assignment;
import com.example.learningplatform.common.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findAllByLessonIdOrderByIdAsc(Long lessonId);

    default Assignment getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Задание", id));
    }
}
