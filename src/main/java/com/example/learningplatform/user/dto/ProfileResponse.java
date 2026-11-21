package com.example.learningplatform.user.dto;

import com.example.learningplatform.user.entity.Profile;

public record ProfileResponse(Long userId, String bio, String avatarUrl, String phone) {

    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(profile.getUser().getId(), profile.getBio(), profile.getAvatarUrl(), profile.getPhone());
    }

    public static ProfileResponse empty(Long userId) {
        return new ProfileResponse(userId, null, null, null);
    }
}
