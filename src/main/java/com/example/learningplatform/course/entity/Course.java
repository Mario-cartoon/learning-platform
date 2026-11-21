package com.example.learningplatform.course.entity;

import com.example.learningplatform.common.entity.BaseEntity;
import com.example.learningplatform.enrollment.entity.Enrollment;
import com.example.learningplatform.review.entity.CourseReview;
import com.example.learningplatform.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseEntity {

    @Setter
    @Column(nullable = false, length = 200)
    private String title;

    @Setter
    @Column(length = 4000)
    private String description;

    @Setter
    @Column(name = "duration_hours")
    private Integer durationHours;

    @Setter
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    // курс владеет своей структурой: удалили курс -> удалились модули, уроки, задания, тесты
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<CourseModule> modules = new ArrayList<>();

    // без каскада: курс с записанными студентами удалять нельзя (проверка в CourseService)
    @OneToMany(mappedBy = "course")
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    private List<CourseReview> reviews = new ArrayList<>();

    // REMOVE тут специально нет, иначе удаление курса удалит теги у других курсов
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "course_tag",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    public Course(String title, String description, Category category, User teacher) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.teacher = teacher;
    }

    public void addModule(CourseModule module) {
        module.setCourse(this);
        module.setOrderIndex(modules.size() + 1);
        modules.add(module);
    }

    public void removeModule(CourseModule module) {
        modules.remove(module);
        for (int i = 0; i < modules.size(); i++) {
            modules.get(i).setOrderIndex(i + 1);
        }
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getCourses().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getCourses().remove(this);
    }

    public void replaceTags(Set<Tag> newTags) {
        new HashSet<>(tags).forEach(this::removeTag);
        newTags.forEach(this::addTag);
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
