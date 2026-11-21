package com.example.learningplatform.user.entity;

import com.example.learningplatform.assignment.entity.Submission;
import com.example.learningplatform.common.entity.BaseEntity;
import com.example.learningplatform.common.exception.BusinessRuleException;
import com.example.learningplatform.course.entity.Course;
import com.example.learningplatform.enrollment.entity.Enrollment;
import com.example.learningplatform.notification.entity.Notification;
import com.example.learningplatform.quiz.entity.QuizSubmission;
import com.example.learningplatform.review.entity.CourseReview;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Setter
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Обратная сторона 1-1. Важно: без bytecode enhancement Hibernate всё равно
    // подгружает её сразу (не знает, null там или нет), см. README
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    @OneToMany(mappedBy = "teacher")
    private List<Course> coursesTaught = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE)
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE)
    private List<Submission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE)
    private List<QuizSubmission> quizSubmissions = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE)
    private List<CourseReview> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.REMOVE)
    private List<Notification> notifications = new ArrayList<>();

    public User(String name, String email, Role role) {
        this.name = name;
        this.role = role;
        setEmail(email);
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public void attachProfile(Profile profile) {
        this.profile = profile;
        profile.setUser(this);
    }

    public void requireRole(Role expected) {
        if (role != expected) {
            throw new BusinessRuleException(String.format(
                    "Операция доступна только для роли %s, у пользователя id=%d роль %s", expected, getId(), role));
        }
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
