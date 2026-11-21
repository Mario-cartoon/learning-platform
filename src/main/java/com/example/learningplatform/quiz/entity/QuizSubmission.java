package com.example.learningplatform.quiz.entity;

import com.example.learningplatform.common.entity.BaseEntity;
import com.example.learningplatform.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Результат прохождения теста. Выбранные ответы не храню - только итоговый балл.
 */
@Getter
@Entity
@Table(name = "quiz_submissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSubmission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /** Процент правильных ответов 0..100. */
    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private boolean passed;

    @Column(name = "taken_at", nullable = false, updatable = false)
    private LocalDateTime takenAt;

    public QuizSubmission(Quiz quiz, User student, int score) {
        this.quiz = quiz;
        this.student = student;
        this.score = score;
        this.passed = score >= quiz.getPassingScore();
    }

    @PrePersist
    void onCreate() {
        takenAt = LocalDateTime.now();
    }
}
