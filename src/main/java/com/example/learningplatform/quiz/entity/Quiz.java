package com.example.learningplatform.quiz.entity;

import com.example.learningplatform.common.entity.BaseEntity;
import com.example.learningplatform.course.entity.CourseModule;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
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

@Getter
@Entity
@Table(name = "quizzes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    /** Минимальный процент правильных ответов, чтобы тест считался сданным. */
    @Column(name = "passing_score", nullable = false)
    private int passingScore;

    // владелец связи 1-1 (тут лежит module_id), unique = один тест на модуль
    @Setter
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false, unique = true)
    private CourseModule module;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.REMOVE)
    private List<QuizSubmission> submissions = new ArrayList<>();

    public Quiz(String title, Integer timeLimitMinutes, int passingScore) {
        this.title = title;
        this.timeLimitMinutes = timeLimitMinutes;
        this.passingScore = passingScore;
    }

    public void addQuestion(Question question) {
        question.setQuiz(this);
        questions.add(question);
    }
}
