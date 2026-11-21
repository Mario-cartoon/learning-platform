package com.example.learningplatform.course.repository;

import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.course.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    default Tag getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Тег", id));
    }
}
