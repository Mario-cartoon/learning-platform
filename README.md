# Learning Platform — учебная платформа на Spring Boot + Hibernate

Итоговый проект по модулю «ORM-фреймворки для Java».

Backend учебной платформы: курсы с модулями и уроками, запись студентов на курсы, домашние задания с проверкой,
тесты с автоматическим подсчётом баллов, отзывы и уведомления. Данные хранятся в PostgreSQL, доступ — через Hibernate / Spring Data JPA,
наружу всё отдаётся через REST API. Протестировать API можно через Swagger UI или через простой веб-интерфейс на главной странице.

## Содержание

- [Стек](#стек)
- [Быстрый старт](#быстрый-старт)
- [Конфигурация и профили](#конфигурация-и-профили)
- [Демо-данные](#демо-данные)
- [Как пользоваться системой](#как-пользоваться-системой)
- [REST API](#rest-api)
- [Архитектура](#архитектура)
- [Модель данных](#модель-данных)
- [Ленивая загрузка](#ленивая-загрузка)
- [Тесты](#тесты)
- [CI и Docker](#ci-и-docker)

## Стек

| | |
|---|---|
| Язык | Java 17 |
| Фреймворк | Spring Boot 3.3 (Web, Data JPA, Validation) |
| ORM | Hibernate 6 |
| БД | PostgreSQL 16 (в тестах — H2 в режиме PostgreSQL и PostgreSQL в Testcontainers) |
| Сборка | Maven |
| Документация API | springdoc-openapi (Swagger UI) |
| Тесты | JUnit 5, AssertJ, Mockito, MockMvc, Testcontainers, JaCoCo |
| Прочее | Lombok, Checkstyle, Docker, GitHub Actions |

## Быстрый старт

### Вариант 1. Всё в Docker (самый простой)

Нужен только Docker.

```bash
git clone <ссылка на репозиторий>
cd learning-platform
docker compose up --build
```

Поднимутся PostgreSQL и приложение, база заполнится демо-данными. После старта:

- http://localhost:8080 — веб-интерфейс: курсы и формы для всех основных сценариев
- http://localhost:8080/swagger-ui.html — Swagger UI, можно сразу дёргать все запросы

Остановить: `docker compose down` (с удалением данных: `docker compose down -v`).

### Вариант 2. Локально через Maven

Нужны JDK 17+ и Maven 3.9+.

1. Поднять PostgreSQL. Либо свой, либо только базу из compose:
   ```bash
   docker compose up -d db
   ```
   Если PostgreSQL свой — создать базу:
   ```sql
   CREATE DATABASE learning_platform;
   ```
2. Запустить приложение (профиль `dev` включён по умолчанию):
   ```bash
   mvn spring-boot:run
   ```
   Если логин/пароль к БД не `postgres/postgres`, передать их через переменные окружения:
   ```bash
   DB_USERNAME=myuser DB_PASSWORD=mypass mvn spring-boot:run
   ```

Таблицы создаются автоматически при старте (`spring.jpa.hibernate.ddl-auto=update`).

## Конфигурация и профили

Параметры подключения в коде не хранятся — только в `application-*.yml` и переменных окружения.

| Файл | Когда используется | Что внутри |
|---|---|---|
| `application.yml` | всегда | общие настройки, `open-in-view: false`, Swagger |
| `application-dev.yml` | по умолчанию, локальная разработка | PostgreSQL на localhost, `ddl-auto: update`, демо-данные включены |
| `application-prod.yml` | запуск в Docker | всё берётся из переменных окружения без значений по умолчанию |
| `src/test/resources/application-test.yml` | тесты | H2 в памяти, `ddl-auto: create-drop` |

Переменные окружения:

| Переменная | По умолчанию (dev) | Описание |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/learning_platform` | JDBC URL |
| `DB_USERNAME` | `postgres` | пользователь БД |
| `DB_PASSWORD` | `postgres` | пароль БД |
| `DEMO_DATA_ENABLED` | `true` в dev, `false` в prod | заполнять ли пустую базу демо-данными |
| `SERVER_PORT` | `8080` | порт приложения |
| `SPRING_PROFILES_ACTIVE` | `dev` | активный профиль |

Для docker compose можно скопировать `.env.example` в `.env` и поменять логин/пароль.

## Демо-данные

При первом запуске на пустой базе `DemoDataInitializer` создаёт данные **через обычные сервисы**,
поэтому заодно проверяются все основные сценарии. Если в базе уже есть пользователи — ничего не делает.

В чистой базе id будут такими:

| id | Пользователь | Роль |
|---|---|---|
| 1 | Ирина Петрова | TEACHER (ведёт «Основы Hibernate») |
| 2 | Олег Смирнов | TEACHER (ведёт «SQL для начинающих») |
| 3 | Анна Кузнецова | STUDENT (записана на оба курса) |
| 4 | Дмитрий Волков | STUDENT (записан на «Основы Hibernate») |
| 5 | Мария Соколова | STUDENT (записана на «SQL для начинающих») |
| 6 | Администратор | ADMIN |

Ещё создаются: 3 категории, 2 курса с тегами (курс 1 — 2 модуля по 2 урока, курс 2 — 1 модуль),
задание к уроку 1, два решения (решение Анны уже проверено), тест к модулю 1 из трёх вопросов
и результат Анны по нему, отзыв Анны на курс 1, уведомления студентам.

## Как пользоваться системой

Есть три способа: веб-интерфейс, Swagger UI или curl/Postman (примеры ниже, в разделе REST API).

### Веб-интерфейс — http://localhost:8080

Простая страница на HTML + JavaScript без фреймворков (`src/main/resources/static/index.html`), работает поверх того же REST API.
Справа на странице журнал запросов: для каждого действия видно метод, URL, код ответа, тело запроса и ответ сервера.

| Вкладка | Что можно сделать |
|---|---|
| Обзор | список курсов, по клику — структура курса (модули, уроки, теги, рейтинг) |
| Пользователи | создать пользователя и категорию, посмотреть и удалить пользователей |
| Курсы | создать курс сразу с модулями и уроками, изменить, удалить, добавить модуль |
| Запись на курс | записать / отписать студента, завершить курс, студенты курса и курсы студента |
| Задания | создать задание к уроку, отправить решение от студента, оценить от преподавателя |
| Тесты | создать тест для модуля, добавить вопросы, пройти тест, посмотреть результаты |
| Отзывы и уведомления | оставить отзыв, посмотреть уведомления пользователя |

Типичный сценарий на демо-данных:

1. **Запись на курс** → курс «Основы Hibernate», студент «Мария Соколова» → «Записать». Повторное нажатие вернёт 409.
2. **Задания** → курс «Основы Hibernate», урок 1.1 → выбрать задание → студент Мария → «Отправить».
3. В блоке «Проверка решений» выбрать решение Марии, преподавателя Ирину, оценку 80 → «Поставить оценку». Оценка 150 вернёт 422.
4. **Отзывы и уведомления** → пользователь Мария → «Показать»: там уведомление о проверке.
5. **Тесты** → курс «Основы Hibernate», модуль 1 → студент Дмитрий → «Открыть тест модуля» → ответить → «Отправить ответы».

### Swagger UI — http://localhost:8080/swagger-ui.html

Все эндпоинты с описанием полей; у нужного запроса нажать **Try it out** → заполнить → **Execute**.

## REST API

Полное описание с примерами — в Swagger UI: http://localhost:8080/swagger-ui.html
(OpenAPI JSON: `/v3/api-docs`).

### Пользователи

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/users?role=STUDENT` | список пользователей, фильтр по роли необязательный |
| GET | `/api/users/{id}` | пользователь |
| POST | `/api/users` | создать |
| PUT | `/api/users/{id}` | изменить |
| DELETE | `/api/users/{id}` | удалить (каскадом удаляются его записи, решения, результаты) |
| GET / PUT | `/api/users/{id}/profile` | профиль (1-1) |

### Категории и теги

| Метод | URL | Описание |
|---|---|---|
| GET, POST | `/api/categories` | список / создать |
| GET, PUT, DELETE | `/api/categories/{id}` | получить / изменить / удалить |
| GET, POST | `/api/tags` | список / создать |
| DELETE | `/api/tags/{id}` | удалить тег (уберётся у всех курсов) |

### Курсы и содержимое

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/courses?categoryId=&tag=` | список курсов, можно фильтровать по категории или тегу |
| GET | `/api/courses/{id}` | курс с полной структурой: модули → уроки, id теста модуля |
| POST | `/api/courses` | создать курс (можно сразу с модулями и уроками) |
| PUT | `/api/courses/{id}` | изменить курс, сменить преподавателя, категорию, теги |
| DELETE | `/api/courses/{id}` | удалить курс со всей структурой (если нет записанных студентов) |
| POST | `/api/courses/{id}/modules` | добавить модуль |
| PUT, DELETE | `/api/modules/{id}` | изменить / удалить модуль |
| GET, POST | `/api/modules/{id}/lessons` | уроки модуля / добавить урок |
| GET, PUT, DELETE | `/api/lessons/{id}` | урок |

### Запись на курс

| Метод | URL | Описание |
|---|---|---|
| POST | `/api/courses/{courseId}/enroll?studentId=` | записать студента |
| DELETE | `/api/courses/{courseId}/enroll?studentId=` | отписать |
| PATCH | `/api/courses/{courseId}/enroll/complete?studentId=` | отметить курс пройденным |
| GET | `/api/courses/{courseId}/students` | студенты курса |
| GET | `/api/users/{studentId}/courses` | курсы студента |

### Задания и решения

| Метод | URL | Описание |
|---|---|---|
| GET, POST | `/api/lessons/{lessonId}/assignments` | задания урока / создать задание |
| GET, PUT, DELETE | `/api/assignments/{id}` | задание |
| POST | `/api/assignments/{id}/submit` | отправить решение |
| GET | `/api/assignments/{id}/submissions` | все решения по заданию (для преподавателя) |
| PUT | `/api/submissions/{id}/grade` | оценить решение |
| GET | `/api/users/{studentId}/submissions` | решения студента с оценками |

### Тесты (quiz)

| Метод | URL | Описание |
|---|---|---|
| POST | `/api/modules/{moduleId}/quiz` | создать тест для модуля (сразу с вопросами или без) |
| GET | `/api/quizzes/{id}` | тест с вопросами и вариантами (без правильных ответов) |
| POST | `/api/quizzes/{id}/questions` | добавить вопрос |
| DELETE | `/api/quizzes/{id}` | удалить тест |
| POST | `/api/quizzes/{id}/take` | пройти тест |
| GET | `/api/quizzes/{id}/results` | результаты по тесту |
| GET | `/api/users/{studentId}/quiz-results` | результаты студента |

### Отзывы и уведомления

| Метод | URL | Описание |
|---|---|---|
| GET, POST | `/api/courses/{courseId}/reviews` | отзывы со средней оценкой / оставить отзыв |
| DELETE | `/api/reviews/{id}` | удалить отзыв |
| GET | `/api/users/{userId}/notifications` | уведомления пользователя |
| PATCH | `/api/notifications/{id}/read` | отметить прочитанным |

### Коды ответов и ошибки

| Код | Когда |
|---|---|
| 200 / 201 / 204 | успех / создано / удалено |
| 400 | не прошла валидация, кривой JSON, нет обязательного параметра |
| 404 | объект не найден |
| 409 | дубликат (уже записан, уже сдал решение, email занят) или удаление запрещено |
| 422 | нарушено бизнес-правило (не та роль, студент не записан на курс, дедлайн прошёл, оценка больше максимума) |

Все ошибки обрабатываются в одном месте — `GlobalExceptionHandler` (`@RestControllerAdvice`) и приходят в одном формате:

```json
{
  "timestamp": "2026-11-20T18:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Ошибка валидации входных данных",
  "path": "/api/courses",
  "fieldErrors": {
    "title": "Название курса обязательно",
    "teacherId": "Нужно указать преподавателя"
  }
}
```

### Пример сценария через curl (на демо-данных)

```bash
# 1. Курс со структурой
curl http://localhost:8080/api/courses/1

# 2. Создать курс сразу с модулем и уроком
curl -X POST http://localhost:8080/api/courses -H "Content-Type: application/json" -d '{
  "title": "Spring Boot с нуля", "description": "Первый REST-сервис",
  "categoryId": 1, "teacherId": 1, "durationHours": 20,
  "tags": ["Java", "Spring"],
  "modules": [{"title": "Старт", "lessons": [{"title": "Spring Initializr", "content": "Создаём проект"}]}]
}'

# 3. Записать Марию (id=5) на курс 1
curl -X POST "http://localhost:8080/api/courses/1/enroll?studentId=5"

# 4. Повторная запись -> 409
curl -X POST "http://localhost:8080/api/courses/1/enroll?studentId=5"

# 5. Мария отправляет решение задания 1
curl -X POST http://localhost:8080/api/assignments/1/submit -H "Content-Type: application/json" \
  -d '{"studentId": 5, "content": "https://github.com/maria/hw1"}'

# 6. Преподаватель (id=1) ставит оценку
curl -X PUT http://localhost:8080/api/submissions/3/grade -H "Content-Type: application/json" \
  -d '{"teacherId": 1, "score": 80, "feedback": "Хорошо"}'

# 7. Дмитрий (id=4) проходит тест 1: {"id вопроса": [id выбранных вариантов]}
curl http://localhost:8080/api/quizzes/1
curl -X POST http://localhost:8080/api/quizzes/1/take -H "Content-Type: application/json" \
  -d '{"studentId": 4, "answers": {"1": [1], "2": [4, 5], "3": [9]}}'

# 8. Результаты и уведомления
curl http://localhost:8080/api/quizzes/1/results
curl http://localhost:8080/api/users/5/notifications
```

## Архитектура

Проект разбит **по модулям предметной области** (package by feature), а внутри модуля — по слоям.
Так всё, что относится, например, к тестам, лежит рядом, и модуль легко найти и поменять.

```
com.example.learningplatform
├── common         общее: BaseEntity, исключения, GlobalExceptionHandler, конфиг Swagger
├── user           пользователи и профили
├── course         категории, теги, курсы, модули, уроки
├── enrollment     запись на курсы + EnrollmentPolicy (проверка "студент записан")
├── assignment     задания, решения, события (AssignmentCreated, SubmissionGraded)
├── quiz           тесты, вопросы, варианты, результаты, стратегии оценивания
├── review         отзывы о курсах
├── notification   уведомления (подписаны на события других модулей)
└── init           DemoDataInitializer
```

Внутри каждого модуля одинаковая структура:

| Пакет | Что лежит |
|---|---|
| `entity` | JPA-сущности и enum'ы. Простые бизнес-правила живут прямо в сущностях (`Submission.grade()`, `Enrollment.complete()`, `User.requireRole()`) |
| `repository` | интерфейсы Spring Data JPA, свои запросы через `@Query` / `@EntityGraph` / derived queries |
| `service` | бизнес-логика и транзакции; наружу отдаёт только DTO |
| `dto` | `record`'ы запросов (с валидацией) и ответов (с фабричным методом `from(entity)`) |
| `web` | `@RestController`, только приём запроса и вызов сервиса |

### Какие паттерны и принципы использовала

- **Layered architecture** — контроллер → сервис → репозиторий, контроллеры не работают с сущностями напрямую.
- **Repository** — Spring Data JPA. В репозиториях есть `default`-метод `getOrThrow(id)`, чтобы не писать `orElseThrow` в каждом сервисе.
- **DTO + Static Factory Method** — `CourseDetailsResponse.from(course, modules)` и т.п. Сущности не уходят в JSON, поэтому нет ни циклов сериализации, ни случайной ленивой загрузки в контроллере.
- **Strategy** — оценивание вопросов теста: `GradingStrategy` + `SingleChoiceGradingStrategy` и `MultipleChoiceGradingStrategy` (с частичным зачётом). Нужную стратегию выбирает `GradingStrategyResolver`, Spring сам передаёт ему все реализации. Новый тип вопроса = новый класс, старый код не трогается.
- **Factory** — `QuestionFactory` создаёт вопрос с вариантами и проверяет его корректность.
- **Observer** (Spring `ApplicationEvent`) — `AssignmentService` и `SubmissionService` публикуют события, `NotificationEventListener` создаёт уведомления. Модуль заданий ничего не знает про уведомления.
- **SOLID**:
  - S — сервисы разделены по ответственности: `CourseService` (курс) и `CourseContentService` (модули/уроки), `QuizService` (редактирование теста) и `QuizAttemptService` (прохождение), подсчёт балла вынесен в `QuizScoreCalculator`;
  - O — стратегии оценивания и слушатели событий расширяются без изменения существующих классов;
  - L — все стратегии взаимозаменяемы через интерфейс;
  - I — `EnrollmentPolicy` даёт другим модулям только одну нужную им проверку, а не весь `EnrollmentService`;
  - D — зависимости только через конструктор (`@RequiredArgsConstructor`), сервисы зависят от интерфейсов репозиториев и стратегий.
- **KISS** — нет лишних абстракций: без MapStruct (маппинг в одну строку в `record`), без отдельного слоя «use case», без своих базовых generic-сервисов.

## Модель данных

16 сущностей: `User`, `Profile`, `Category`, `Tag`, `Course`, `CourseModule`, `Lesson`, `Enrollment`,
`Assignment`, `Submission`, `Quiz`, `Question`, `AnswerOption`, `QuizSubmission`, `CourseReview`, `Notification`.

ER-диаграмма, типы связей, ограничения уникальности и правила каскадного удаления описаны в [docs/database.md](docs/database.md).

Коротко:
- **1-1**: `User — Profile`, `CourseModule — Quiz`;
- **1-M**: `Course → CourseModule → Lesson → Assignment → Submission`, `Quiz → Question → AnswerOption`, `Category → Course`, `User(teacher) → Course`;
- **M-M через сущность**: `Enrollment`, `Submission`, `QuizSubmission`, `CourseReview` (нужны дополнительные поля);
- **M-M без сущности**: `Course — Tag` через таблицу `course_tag`.

Модуль курса называется `CourseModule`, а не `Module`, потому что `Module` конфликтует с `java.lang.Module`.

## Ленивая загрузка

Все связи в проекте ленивые: у коллекций это так по умолчанию, а у `@ManyToOne` / `@OneToOne` прописала `fetch = FetchType.LAZY` явно.
Плюс выключила `spring.jpa.open-in-view`, чтобы сессия Hibernate не жила до конца HTTP-запроса и проблемы не прятались.

**Что получается.** Если загрузить курс через `courseRepository.findById(id)` и потом вне транзакции вызвать
`course.getModules().size()` — будет `LazyInitializationException`. С преподавателем так же: `course.getTeacher()` — это прокси,
`getId()` работает (id известен без запроса), а `getName()` уже падает. Это проверяется в `LazyLoadingTest`.

**Как решала:**

1. **DTO внутри транзакции** — основной способ. Сервисы помечены `@Transactional(readOnly = true)`,
   всё нужное достают внутри метода и возвращают `record`-DTO. Контроллер получает уже готовые данные.
2. **`JOIN FETCH`** — `CourseModuleRepository.findAllWithLessonsByCourseId`: модули курса сразу с уроками и тестом одним запросом.
3. **`@EntityGraph`** — `CourseRepository.findWithDetailsById` (категория, преподаватель, теги),
   а в `EnrollmentRepository`, `SubmissionRepository`, `QuizSubmissionRepository` — чтобы при выводе списков не было N+1.
4. **`Hibernate.initialize()`** внутри транзакции — показано в тесте как ещё один вариант.

**Что выяснила по ходу:**

- Курс + модули + уроки одним `JOIN FETCH` не загрузить: это две коллекции `List` (bag), Hibernate бросает
  `MultipleBagFetchException`. Поэтому структура курса грузится двумя запросами: курс с категорией/преподавателем/тегами и отдельно модули с уроками.
- Обратная сторона `@OneToOne(mappedBy = ...)` (`User.profile`, `CourseModule.quiz`) на самом деле не ленивая,
  несмотря на `fetch = LAZY`: Hibernate не знает, `null` там или нет, поэтому делает дополнительный запрос.
  Для модулей это закрыто `left join fetch m.quiz`, для профиля оставила так (по-настоящему лениво можно сделать только через bytecode enhancement или перенеся FK в `users`).
- `open-in-view=true` (значение по умолчанию в Spring Boot) прячет эти ошибки, но держит соединение с БД весь запрос — поэтому выключено.

## Тесты

```bash
mvn test        # все тесты
mvn verify      # тесты + checkstyle + отчёт о покрытии target/site/jacoco/index.html
```

Интеграционные тесты поднимают весь контекст Spring (`@SpringBootTest`) на H2 в режиме PostgreSQL,
схема создаётся Hibernate заново. `PostgresContainerTest` прогоняет то же самое на настоящем PostgreSQL
через Testcontainers — если Docker не запущен, он просто пропускается (в CI выполняется).

Тесты специально не `@Transactional` (иначе не воспроизвести `LazyInitializationException`),
поэтому после каждого теста таблицы очищаются в `AbstractIntegrationTest`.

| Класс | Тип | Что проверяет |
|---|---|---|
| `SchemaCreationTest` | интеграционный | все таблицы и внешние ключи создаются, уникальные ограничения работают |
| `CourseRepositoryTest` | интеграционный | CRUD курса, каскадное сохранение и удаление структуры, ManyToMany с тегами |
| `LazyLoadingTest` | интеграционный | `LazyInitializationException` вне транзакции и 4 способа её избежать |
| `UserServiceTest` | интеграционный | пользователь + профиль, уникальный email, каскадное удаление, запрет удаления преподавателя |
| `EnrollmentServiceTest` | интеграционный | запись, повторная запись, отписка, списки студентов и курсов, запрет удаления курса со студентами |
| `SubmissionServiceTest` | интеграционный | отправка и оценка, повторная отправка, дедлайн, чужой преподаватель, уведомления |
| `QuizServiceTest` | интеграционный | создание теста, прохождение, частичный зачёт, сохранение результатов |
| `CourseControllerTest`, `EnrollmentControllerTest` | MockMvc | HTTP-коды 201/204/400/404/409/422, формат ошибок, JSON-ответы |
| `PostgresContainerTest` | Testcontainers | схема и сохранение курса в настоящем PostgreSQL |
| `GradingStrategiesTest` | юнит | стратегии оценивания и подсчёт итогового процента |
| `DomainRulesTest` | юнит | бизнес-правила в сущностях (оценка, дедлайн, нумерация модулей, синхронизация M-M) |
| `EnrollmentServiceUnitTest` | юнит (Mockito) | логика записи на курс без базы |

Всего 74 теста.

## CI и Docker

- `Dockerfile` — двухэтапная сборка: Maven собирает jar, в итоговый образ попадает только JRE + jar.
- `docker-compose.yml` — PostgreSQL + приложение, приложение ждёт healthcheck базы.
- `.github/workflows/ci.yml` — на каждый push и pull request: checkstyle → сборка → все тесты (включая Testcontainers) → отчёт JaCoCo в артефакты → сборка Docker-образа.
- `checkstyle.xml` — небольшой набор правил (неиспользуемые импорты, длина строки, фигурные скобки и т.п.), проверка запускается на фазе `validate`, то есть при любой сборке.

## Что можно улучшить

- Spring Security + JWT, чтобы роль бралась из токена, а не из `studentId` / `teacherId` в запросе.
- Миграции через Flyway вместо `ddl-auto=update`.
- Пагинация для списков курсов и решений.
- Хранить выбранные студентом ответы в тесте, чтобы показывать разбор ошибок.
