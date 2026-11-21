package com.example.learningplatform.course.entity;

import com.example.learningplatform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // ManyToMany без промежуточной сущности, владелец связи - Course
    @ManyToMany(mappedBy = "tags")
    private Set<Course> courses = new HashSet<>();

    public Tag(String name) {
        this.name = name;
    }
}
