package com.example.learningplatform.course.entity;

import com.example.learningplatform.common.entity.BaseEntity;
import com.example.learningplatform.quiz.entity.Quiz;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Модуль курса. Назвала CourseModule, потому что Module конфликтует с java.lang.Module.
 */
@Getter
@Entity
@Table(name = "modules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseModule extends BaseEntity {

    @Setter
    @Column(nullable = false, length = 200)
    private String title;

    @Setter
    @Column(length = 2000)
    private String description;

    @Setter(AccessLevel.PACKAGE)
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Lesson> lessons = new ArrayList<>();

    // один модуль - не более одного теста
    @OneToOne(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Quiz quiz;

    public CourseModule(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public void addLesson(Lesson lesson) {
        lesson.setModule(this);
        lesson.setOrderIndex(lessons.size() + 1);
        lessons.add(lesson);
    }

    public void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
        for (int i = 0; i < lessons.size(); i++) {
            lessons.get(i).setOrderIndex(i + 1);
        }
    }

    public void attachQuiz(Quiz quiz) {
        this.quiz = quiz;
        quiz.setModule(this);
    }
}
