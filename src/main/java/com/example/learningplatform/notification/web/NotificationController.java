package com.example.learningplatform.notification.web;

import com.example.learningplatform.notification.dto.NotificationResponse;
import com.example.learningplatform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/api/users/{userId}/notifications")
    public List<NotificationResponse> findByUser(@PathVariable Long userId) {
        return notificationService.findByUser(userId);
    }

    @PatchMapping("/api/notifications/{id}/read")
    public NotificationResponse markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }
}
