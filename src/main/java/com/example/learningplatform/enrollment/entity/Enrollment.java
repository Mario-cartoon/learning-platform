package com.example.learningplatform.enrollment.entity;

import com.example.learningplatform.common.entity.BaseEntity;
import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.course.entity.Course;
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
 * Запись студента на курс: связь многие-ко-многим User <-> Course с доп. полями.
 */
@Getter
@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(name = "uk_enrollment_student_course",
                columnNames = {"student_id", "course_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    public Enrollment(User student, Course course) {
        this.student = student;
        this.course = course;
        this.status = EnrollmentStatus.ACTIVE;
    }

    public void complete() {
        if (status == EnrollmentStatus.COMPLETED) {
            throw new BusinessRuleException("Курс уже отмечен как завершённый");
        }
        status = EnrollmentStatus.COMPLETED;
        completedAt = LocalDateTime.now();
    }

    @PrePersist
    void onCreate() {
        enrolledAt = LocalDateTime.now();
    }
}
