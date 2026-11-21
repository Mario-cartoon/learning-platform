package com.example.learningplatform.course.repository;

import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.course.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    default Category getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Категория", id));
    }
}
