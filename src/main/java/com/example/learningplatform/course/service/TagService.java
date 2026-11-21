package com.example.learningplatform.course.service;

import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.course.dto.TagRequest;
import com.example.learningplatform.course.dto.TagResponse;
import com.example.learningplatform.course.entity.Tag;
import com.example.learningplatform.course.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> findAll() {
        return tagRepository.findAll(Sort.by("name")).stream().map(TagResponse::from).toList();
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        String name = request.name().trim();
        if (tagRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new ConflictException("Тег '" + name + "' уже существует");
        }
        return TagResponse.from(tagRepository.save(new Tag(name)));
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.getOrThrow(id);
        // Course - владелец связи, поэтому строки в course_tag чистим через курсы
        new HashSet<>(tag.getCourses()).forEach(course -> course.removeTag(tag));
        tagRepository.delete(tag);
        log.info("Удалён тег id={}", id);
    }

    /**
     * Находит существующие теги по именам, недостающие создаёт (сохранятся каскадом вместе с курсом).
     */
    public Set<Tag> resolveTags(Collection<String> names) {
        if (names == null) {
            return Set.of();
        }
        return names.stream()
                .map(String::trim)
                .distinct()
                .map(name -> tagRepository.findByNameIgnoreCase(name).orElseGet(() -> new Tag(name)))
                .collect(Collectors.toSet());
    }
}
