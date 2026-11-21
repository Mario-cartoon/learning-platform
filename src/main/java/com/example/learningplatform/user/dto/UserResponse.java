package com.example.learningplatform.user.dto;

import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;

import java.time.LocalDateTime;

public record UserResponse(Long id, String name, String email, Role role, LocalDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
