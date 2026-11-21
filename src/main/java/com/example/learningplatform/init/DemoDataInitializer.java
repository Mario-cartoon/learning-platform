package com.example.learningplatform.init;

import com.example.learningplatform.assignment.dto.AssignmentRequest;
import com.example.learningplatform.assignment.dto.AssignmentResponse;
import com.example.learningplatform.assignment.dto.GradeRequest;
import com.example.learningplatform.assignment.dto.SubmissionRequest;
import com.example.learningplatform.assignment.dto.SubmissionResponse;
import com.example.learningplatform.assignment.service.AssignmentService;
import com.example.learningplatform.assignment.service.SubmissionService;
import com.example.learningplatform.course.dto.CategoryRequest;
import com.example.learningplatform.course.dto.CourseDetailsResponse;
import com.example.learningplatform.course.dto.CourseRequest;
import com.example.learningplatform.course.dto.LessonRequest;
import com.example.learningplatform.course.dto.ModuleRequest;
import com.example.learningplatform.course.service.CategoryService;
import com.example.learningplatform.course.service.CourseService;
import com.example.learningplatform.enrollment.service.EnrollmentService;
import com.example.learningplatform.quiz.dto.OptionRequest;
import com.example.learningplatform.quiz.dto.QuestionRequest;
import com.example.learningplatform.quiz.dto.QuizRequest;
import com.example.learningplatform.quiz.dto.QuizResponse;
import com.example.learningplatform.quiz.dto.TakeQuizRequest;
import com.example.learningplatform.quiz.entity.QuestionType;
import com.example.learningplatform.quiz.service.QuizAttemptService;
import com.example.learningplatform.quiz.service.QuizService;
import com.example.learningplatform.review.dto.ReviewRequest;
import com.example.learningplatform.review.service.ReviewService;
import com.example.learningplatform.user.dto.ProfileRequest;
import com.example.learningplatform.user.dto.UserRequest;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.repository.UserRepository;
import com.example.learningplatform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Заполняет пустую базу демонстрационными данными через обычные сервисы,
 * то есть заодно прогоняет основные сценарии. Включается свойством app.demo-data.enabled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;
    private final ReviewService reviewService;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("В базе уже есть данные, демо-данные не загружаю");
            return;
        }
        log.info("Загружаю демо-данные...");

        Long irina = createUser("Ирина Петрова", "irina.petrova@edu.local", Role.TEACHER);
        Long oleg = createUser("Олег Смирнов", "oleg.smirnov@edu.local", Role.TEACHER);
        Long anna = createUser("Анна Кузнецова", "anna.kuznetsova@edu.local", Role.STUDENT);
        Long dmitry = createUser("Дмитрий Волков", "dmitry.volkov@edu.local", Role.STUDENT);
        Long maria = createUser("Мария Соколова", "maria.sokolova@edu.local", Role.STUDENT);
        createUser("Администратор", "admin@edu.local", Role.ADMIN);
        userService.updateProfile(irina, new ProfileRequest(
                "Java-разработчик, 8 лет в коммерческой разработке", null, "+7 900 000-00-01"));

        Long programming = categoryService.create(new CategoryRequest("Программирование")).id();
        Long databases = categoryService.create(new CategoryRequest("Базы данных")).id();
        categoryService.create(new CategoryRequest("Дизайн"));

        CourseDetailsResponse hibernate = createHibernateCourse(programming, irina);
        CourseDetailsResponse sql = createSqlCourse(databases, oleg);

        enrollmentService.enroll(hibernate.id(), anna);
        enrollmentService.enroll(hibernate.id(), dmitry);
        enrollmentService.enroll(sql.id(), maria);
        enrollmentService.enroll(sql.id(), anna);

        Long firstLessonId = hibernate.modules().get(0).lessons().get(0).id();
        AssignmentResponse assignment = assignmentService.create(firstLessonId, new AssignmentRequest(
                "Настроить подключение к PostgreSQL",
                "Создайте Spring Boot проект, подключите PostgreSQL, параметры вынесите в application.yml",
                LocalDateTime.now().plusDays(14), 100));
        SubmissionResponse submission = submissionService.submit(assignment.id(),
                new SubmissionRequest(anna, "https://github.com/anna/hibernate-hw1"));
        submissionService.grade(submission.id(),
                new GradeRequest(irina, 90, "Хорошо. Пароль лучше вынести в переменную окружения"));
        submissionService.submit(assignment.id(),
                new SubmissionRequest(dmitry, "Подключение настроил, скриншот запуска в репозитории"));

        Long firstModuleId = hibernate.modules().get(0).id();
        QuizResponse quiz = createQuiz(firstModuleId);
        quizAttemptService.takeQuiz(quiz.id(), new TakeQuizRequest(anna, Map.of(
                quiz.questions().get(0).id(), Set.of(optionId(quiz, 0, "Object-Relational Mapping")),
                quiz.questions().get(1).id(), Set.of(optionId(quiz, 1, "@OneToMany"), optionId(quiz, 1, "@ManyToMany")),
                quiz.questions().get(2).id(), Set.of(optionId(quiz, 2, "LAZY")))));

        reviewService.addReview(hibernate.id(), new ReviewRequest(anna, 5,
                "Наконец-то разобралась, откуда берётся LazyInitializationException"));

        log.info("Демо-данные загружены. Swagger UI: http://localhost:8080/swagger-ui.html");
    }

    private Long createUser(String name, String email, Role role) {
        return userService.create(new UserRequest(name, email, role)).id();
    }

    private CourseDetailsResponse createHibernateCourse(Long categoryId, Long teacherId) {
        return courseService.create(new CourseRequest(
                "Основы Hibernate",
                "ORM в Java: сущности, связи, ленивая загрузка, Spring Data JPA",
                categoryId, teacherId, 24, LocalDate.now().plusDays(7),
                Set.of("Java", "Hibernate", "Beginner"),
                List.of(
                        new ModuleRequest("Введение в ORM", "Зачем нужен ORM и как подключить его к проекту", List.of(
                                new LessonRequest("Что такое ORM",
                                        "ORM связывает Java-объекты с таблицами базы данных...", null),
                                new LessonRequest("Spring Data JPA",
                                        "Репозитории, derived queries, @Query",
                                        "https://example.com/video/spring-data-jpa"))),
                        new ModuleRequest("Связи между сущностями", "1-1, 1-M, M-M и ленивая загрузка", List.of(
                                new LessonRequest("OneToMany и ManyToOne", "Владелец связи, mappedBy, каскады", null),
                                new LessonRequest("Ленивая загрузка",
                                        "FetchType.LAZY, JOIN FETCH, @EntityGraph, LazyInitializationException",
                                        null))))));
    }

    private CourseDetailsResponse createSqlCourse(Long categoryId, Long teacherId) {
        return courseService.create(new CourseRequest(
                "SQL для начинающих",
                "Запросы SELECT, JOIN, группировки и индексы на примере PostgreSQL",
                categoryId, teacherId, 16, LocalDate.now().plusDays(14),
                Set.of("SQL", "PostgreSQL", "Beginner"),
                List.of(new ModuleRequest("Основы SQL", null, List.of(
                        new LessonRequest("SELECT и WHERE", "Выборка и фильтрация данных", null),
                        new LessonRequest("JOIN", "INNER, LEFT, RIGHT, FULL JOIN", null))))));
    }

    private QuizResponse createQuiz(Long moduleId) {
        return quizService.create(moduleId, new QuizRequest("Тест по модулю «Введение в ORM»", 15, 60, List.of(
                new QuestionRequest("Как расшифровывается ORM?", QuestionType.SINGLE_CHOICE, List.of(
                        new OptionRequest("Object-Relational Mapping", true),
                        new OptionRequest("Online Resource Manager", false),
                        new OptionRequest("Ordered Row Model", false))),
                new QuestionRequest("Какие аннотации описывают связь с коллекцией?", QuestionType.MULTIPLE_CHOICE,
                        List.of(
                                new OptionRequest("@OneToMany", true),
                                new OptionRequest("@ManyToMany", true),
                                new OptionRequest("@ManyToOne", false),
                                new OptionRequest("@Column", false))),
                new QuestionRequest("Какой FetchType по умолчанию у @OneToMany?", QuestionType.SINGLE_CHOICE, List.of(
                        new OptionRequest("EAGER", false),
                        new OptionRequest("LAZY", true))))));
    }

    private Long optionId(QuizResponse quiz, int questionIndex, String text) {
        return quiz.questions().get(questionIndex).options().stream()
                .filter(option -> option.text().equals(text))
                .findFirst()
                .orElseThrow()
                .id();
    }
}
