package com.example.learningplatform.notification.dto;

import com.example.learningplatform.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, String message, boolean read, LocalDateTime createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getMessage(),
                notification.isRead(), notification.getCreatedAt());
    }
}
