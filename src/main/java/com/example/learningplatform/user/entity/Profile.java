package com.example.learningplatform.user.entity;

import com.example.learningplatform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Редко используемые данные пользователя вынесены в отдельную таблицу.
 */
@Getter
@Setter
@Entity
@Table(name = "profiles")
@NoArgsConstructor
public class Profile extends BaseEntity {

    @Setter(AccessLevel.PACKAGE)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 2000)
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 30)
    private String phone;
}
