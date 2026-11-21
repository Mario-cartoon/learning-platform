package com.example.learningplatform.course.service;

import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.course.dto.CourseDetailsResponse;
import com.example.learningplatform.course.dto.CourseRequest;
import com.example.learningplatform.course.dto.CourseSummaryResponse;
import com.example.learningplatform.course.dto.ModuleRequest;
import com.example.learningplatform.course.entity.Category;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.course.entity.CourseModule;
import com.example.learningplatform.course.repository.CategoryRepository;
import com.example.learningplatform.course.repository.CourseModuleRepository;
import com.example.learningplatform.course.repository.CourseRepository;
import com.example.learningplatform.enrollment.repository.EnrollmentRepository;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;
import com.example.learningplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TagService tagService;

    public List<CourseSummaryResponse> findAll(Long categoryId, String tag) {
        List<Course> courses;
        if (categoryId != null) {
            courses = courseRepository.findAllByCategoryIdWithTeacher(categoryId);
        } else if (tag != null && !tag.isBlank()) {
            courses = courseRepository.findAllByTagName(tag.trim());
        } else {
            courses = courseRepository.findAllWithCategoryAndTeacher();
        }
        return courses.stream().map(CourseSummaryResponse::from).toList();
    }

    /**
     * Полная структура курса. Всё нужное подгружается явными запросами внутри транзакции,
     * наружу уходит DTO - поэтому LazyInitializationException в контроллере не будет.
     */
    public CourseDetailsResponse getDetails(Long id) {
        Course course = courseRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Курс", id));
        List<CourseModule> modules = moduleRepository.findAllWithLessonsByCourseId(id);
        return CourseDetailsResponse.from(course, modules);
    }

    @Transactional
    public CourseDetailsResponse create(CourseRequest request) {
        Course course = new Course(request.title(), request.description(),
                findCategory(request.categoryId()), findTeacher(request.teacherId()));
        course.setDurationHours(request.durationHours());
        course.setStartDate(request.startDate());
        course.replaceTags(tagService.resolveTags(request.tags()));
        if (request.modules() != null) {
            request.modules().stream().map(ModuleRequest::toEntity).forEach(course::addModule);
        }
        Course saved = courseRepository.save(course);
        log.info("Создан курс id={} '{}', модулей: {}", saved.getId(), saved.getTitle(), saved.getModules().size());
        return CourseDetailsResponse.from(saved, saved.getModules());
    }

    @Transactional
    public CourseDetailsResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.getOrThrow(id);
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setDurationHours(request.durationHours());
        course.setStartDate(request.startDate());
        course.setCategory(findCategory(request.categoryId()));
        course.setTeacher(findTeacher(request.teacherId()));
        course.replaceTags(tagService.resolveTags(request.tags()));
        log.info("Обновлён курс id={}", id);
        return getDetails(id);
    }

    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.getOrThrow(id);
        // Решила не удалять курс с записанными студентами, чтобы не потерять их прогресс
        if (enrollmentRepository.existsByCourseId(id)) {
            throw new ConflictException("Нельзя удалить курс, на который записаны студенты. Сначала отпишите их");
        }
        courseRepository.delete(course);
        log.info("Удалён курс id={} вместе с модулями, уроками, заданиями и тестами", id);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.getOrThrow(categoryId);
    }

    private User findTeacher(Long teacherId) {
        User teacher = userRepository.getOrThrow(teacherId);
        teacher.requireRole(Role.TEACHER);
        return teacher;
    }
}
