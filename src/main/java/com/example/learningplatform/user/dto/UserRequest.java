package com.example.learningplatform.user.dto;

import com.example.learningplatform.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Имя обязательно")
        @Size(max = 100, message = "Имя не длиннее 100 символов")
        String name,

        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный email")
        @Size(max = 150, message = "Email не длиннее 150 символов")
        String email,

        @NotNull(message = "Роль обязательна: STUDENT, TEACHER или ADMIN")
        Role role) {
}
