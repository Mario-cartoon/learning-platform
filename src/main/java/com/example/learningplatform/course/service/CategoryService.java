package com.example.learningplatform.course.service;

import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.course.dto.CategoryRequest;
import com.example.learningplatform.course.dto.CategoryResponse;
import com.example.learningplatform.course.entity.Category;
import com.example.learningplatform.course.repository.CategoryRepository;
import com.example.learningplatform.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll(Sort.by("name")).stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(categoryRepository.getOrThrow(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        ensureNameIsFree(request.name(), null);
        Category saved = categoryRepository.save(new Category(request.name().trim()));
        log.info("Создана категория id={} '{}'", saved.getId(), saved.getName());
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.getOrThrow(id);
        ensureNameIsFree(request.name(), id);
        category.setName(request.name().trim());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.getOrThrow(id);
        if (courseRepository.existsByCategoryId(id)) {
            throw new ConflictException("Нельзя удалить категорию, в которой есть курсы");
        }
        categoryRepository.delete(category);
        log.info("Удалена категория id={}", id);
    }

    private void ensureNameIsFree(String name, Long currentId) {
        categoryRepository.findByNameIgnoreCase(name.trim())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Категория '" + name + "' уже существует");
                });
    }
}
