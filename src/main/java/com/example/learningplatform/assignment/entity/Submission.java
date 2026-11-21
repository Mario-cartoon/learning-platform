package com.example.learningplatform.assignment.entity;

import com.example.learningplatform.common.entity.BaseEntity;
import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Решение студента. Одно решение на задание от одного студента - проверка в сервисе + unique в БД.
 */
@Getter
@Entity
@Table(name = "submissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_submission_student_assignment",
                columnNames = {"student_id", "assignment_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    private Integer score;

    @Column(length = 2000)
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    public Submission(Assignment assignment, User student, String content) {
        this.assignment = assignment;
        this.student = student;
        this.content = content;
        this.status = SubmissionStatus.SUBMITTED;
    }

    public void grade(int score, String feedback) {
        int max = assignment.getMaxScore();
        if (score < 0 || score > max) {
            throw new BusinessRuleException(String.format("Оценка должна быть в диапазоне от 0 до %d", max));
        }
        this.score = score;
        this.feedback = feedback;
        this.status = SubmissionStatus.GRADED;
        this.gradedAt = LocalDateTime.now();
    }

    @PrePersist
    void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
