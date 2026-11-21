# Модель данных

16 сущностей + одна служебная таблица `course_tag` (ManyToMany без отдельной сущности).

Схему можно вставить в [Mermaid Live Editor](https://mermaid.live), GitHub тоже рисует её прямо в этом файле.

```mermaid
erDiagram
    USERS ||--o| PROFILES : "1-1"
    USERS ||--o{ COURSES : "ведёт (teacher)"
    CATEGORIES ||--o{ COURSES : ""
    COURSES }o--o{ TAGS : "course_tag"
    COURSES ||--o{ MODULES : ""
    MODULES ||--o{ LESSONS : ""
    MODULES ||--o| QUIZZES : "1-1"
    LESSONS ||--o{ ASSIGNMENTS : ""
    ASSIGNMENTS ||--o{ SUBMISSIONS : ""
    USERS ||--o{ SUBMISSIONS : "student"
    USERS ||--o{ ENROLLMENTS : "student"
    COURSES ||--o{ ENROLLMENTS : ""
    QUIZZES ||--o{ QUESTIONS : ""
    QUESTIONS ||--o{ ANSWER_OPTIONS : ""
    QUIZZES ||--o{ QUIZ_SUBMISSIONS : ""
    USERS ||--o{ QUIZ_SUBMISSIONS : "student"
    COURSES ||--o{ COURSE_REVIEWS : ""
    USERS ||--o{ COURSE_REVIEWS : "student"
    USERS ||--o{ NOTIFICATIONS : "recipient"

    USERS {
        bigint id PK
        varchar name
        varchar email UK
        varchar role "STUDENT / TEACHER / ADMIN"
        timestamp created_at
    }
    PROFILES {
        bigint id PK
        bigint user_id FK,UK
        varchar bio
        varchar avatar_url
        varchar phone
    }
    CATEGORIES {
        bigint id PK
        varchar name UK
    }
    TAGS {
        bigint id PK
        varchar name UK
    }
    COURSES {
        bigint id PK
        varchar title
        varchar description
        int duration_hours
        date start_date
        bigint category_id FK
        bigint teacher_id FK
    }
    MODULES {
        bigint id PK
        bigint course_id FK
        varchar title
        int order_index
    }
    LESSONS {
        bigint id PK
        bigint module_id FK
        varchar title
        varchar content
        varchar video_url
        int order_index
    }
    ASSIGNMENTS {
        bigint id PK
        bigint lesson_id FK
        varchar title
        timestamp due_date
        int max_score
    }
    SUBMISSIONS {
        bigint id PK
        bigint assignment_id FK
        bigint student_id FK
        varchar content
        int score "null до проверки"
        varchar status "SUBMITTED / GRADED"
    }
    ENROLLMENTS {
        bigint id PK
        bigint student_id FK
        bigint course_id FK
        varchar status "ACTIVE / COMPLETED"
        timestamp enrolled_at
    }
    QUIZZES {
        bigint id PK
        bigint module_id FK,UK
        varchar title
        int passing_score
    }
    QUESTIONS {
        bigint id PK
        bigint quiz_id FK
        varchar question_text
        varchar question_type "SINGLE_CHOICE / MULTIPLE_CHOICE"
    }
    ANSWER_OPTIONS {
        bigint id PK
        bigint question_id FK
        varchar option_text
        boolean is_correct
    }
    QUIZ_SUBMISSIONS {
        bigint id PK
        bigint quiz_id FK
        bigint student_id FK
        int score "процент"
        boolean passed
    }
    COURSE_REVIEWS {
        bigint id PK
        bigint course_id FK
        bigint student_id FK
        int rating "1..5"
        varchar comment
    }
    NOTIFICATIONS {
        bigint id PK
        bigint recipient_id FK
        varchar message
        boolean is_read
    }
```

## Типы связей

| Связь | Где | Как сделано |
|---|---|---|
| 1-1 | User — Profile | `Profile.user` владеет (`user_id unique`), в `User` — `mappedBy` |
| 1-1 | CourseModule — Quiz | `Quiz.module` владеет (`module_id unique`), один тест на модуль |
| 1-M | Course → Module → Lesson → Assignment → Submission | `@OneToMany(mappedBy)` + `@ManyToOne(fetch = LAZY)` |
| 1-M | Quiz → Question → AnswerOption | то же самое |
| M-M через сущность | User — Course (Enrollment), User — Assignment (Submission), User — Quiz (QuizSubmission), User — Course (CourseReview) | отдельная сущность, потому что нужны свои поля (статус, дата, оценка) |
| M-M без сущности | Course — Tag | `@ManyToMany` + `@JoinTable(name = "course_tag")`, владелец — Course |

Все `@ManyToOne` и `@OneToOne` явно помечены `fetch = FetchType.LAZY` (по умолчанию у них EAGER).
Коллекции `@OneToMany` / `@ManyToMany` ленивые по умолчанию.

## Уникальность

- `users.email`, `categories.name`, `tags.name`
- `enrollments (student_id, course_id)` — нельзя записаться дважды
- `submissions (student_id, assignment_id)` — одно решение на задание
- `course_reviews (student_id, course_id)` — один отзыв на курс
- `quizzes.module_id` — один тест на модуль

Проверки дублируются в сервисах (понятная ошибка 409), а ограничения в БД — страховка на случай гонки.

## Каскады и удаление

- **Курс** владеет своей структурой: `CascadeType.ALL + orphanRemoval` на модули, уроки, задания, решения, тест с вопросами.
  Но курс, на который записаны студенты, удалить нельзя — сервис вернёт 409. Решила так, чтобы случайно не потерять прогресс студентов.
- **Теги** не удаляются вместе с курсом (на `@ManyToMany` нет `REMOVE`), удаляются только строки в `course_tag`.
- **Пользователь**: при удалении каскадом удаляются профиль, записи на курсы, решения, результаты тестов, отзывы и уведомления.
  Преподавателя, у которого есть курсы, удалить нельзя (409).
- **Категория** с курсами не удаляется (409).
