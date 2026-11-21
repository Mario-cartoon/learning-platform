package com.example.learningplatform.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @Size(max = 2000, message = "Био не длиннее 2000 символов")
        String bio,

        @Size(max = 500, message = "Ссылка на аватар не длиннее 500 символов")
        @Pattern(regexp = "^(https?://.*)?$", message = "Ссылка на аватар должна начинаться с http:// или https://")
        String avatarUrl,

        @Size(max = 30)
        @Pattern(regexp = "^[0-9+()\\- ]*$", message = "Телефон может содержать только цифры, пробелы и символы + - ( )")
        String phone) {
}
