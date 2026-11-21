package com.example.learningplatform.course.service;

import com.example.learningplatform.course.dto.LessonRequest;
import com.example.learningplatform.course.dto.LessonResponse;
import com.example.learningplatform.course.dto.ModuleRequest;
import com.example.learningplatform.course.dto.ModuleResponse;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.entity.CourseModule;
import com.example.learningplatform.course.entity.Lesson;
import com.example.learningplatform.course.repository.CourseModuleRepository;
import com.example.learningplatform.course.repository.CourseRepository;
import com.example.learningplatform.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Содержимое курса: модули и уроки. Вынесено из CourseService, чтобы он не разрастался.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseContentService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public ModuleResponse addModule(Long courseId, ModuleRequest request) {
        Course course = courseRepository.getOrThrow(courseId);
        CourseModule module = request.toEntity();
        course.addModule(module);
        moduleRepository.save(module);
        log.info("В курс id={} добавлен модуль id={}", courseId, module.getId());
        return ModuleResponse.from(module);
    }

    @Transactional
    public ModuleResponse updateModule(Long moduleId, ModuleRequest request) {
        CourseModule module = moduleRepository.getOrThrow(moduleId);
        module.setTitle(request.title());
        module.setDescription(request.description());
        return ModuleResponse.from(module);
    }

    @Transactional
    public void deleteModule(Long moduleId) {
        CourseModule module = moduleRepository.getOrThrow(moduleId);
        // orphanRemoval удалит модуль, а каскад - его уроки, задания и тест
        module.getCourse().removeModule(module);
        log.info("Удалён модуль id={}", moduleId);
    }

    public List<LessonResponse> findLessons(Long moduleId) {
        moduleRepository.getOrThrow(moduleId);
        return lessonRepository.findAllByModuleIdOrderByOrderIndexAsc(moduleId).stream()
                .map(LessonResponse::from)
                .toList();
    }

    @Transactional
    public LessonResponse addLesson(Long moduleId, LessonRequest request) {
        CourseModule module = moduleRepository.getOrThrow(moduleId);
        Lesson lesson = request.toEntity();
        module.addLesson(lesson);
        lessonRepository.save(lesson);
        log.info("В модуль id={} добавлен урок id={}", moduleId, lesson.getId());
        return LessonResponse.from(lesson);
    }

    public LessonResponse getLesson(Long lessonId) {
        return LessonResponse.from(lessonRepository.getOrThrow(lessonId));
    }

    @Transactional
    public LessonResponse updateLesson(Long lessonId, LessonRequest request) {
        Lesson lesson = lessonRepository.getOrThrow(lessonId);
        lesson.setTitle(request.title());
        lesson.setContent(request.content());
        lesson.setVideoUrl(request.videoUrl());
        return LessonResponse.from(lesson);
    }

    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.getOrThrow(lessonId);
        lesson.getModule().removeLesson(lesson);
        log.info("Удалён урок id={}", lessonId);
    }
}
