package com.example.learningplatform.notification.service;

import com.example.learningplatform.notification.dto.NotificationResponse;
import com.example.learningplatform.notification.entity.Notification;
import com.example.learningplatform.notification.repository.NotificationRepository;
import com.example.learningplatform.user.entity.User;
import com.example.learningplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notify(User recipient, String message) {
        notificationRepository.save(new Notification(recipient, message));
        log.debug("Уведомление пользователю id={}: {}", recipient.getId(), message);
    }

    public List<NotificationResponse> findByUser(Long userId) {
        userRepository.getOrThrow(userId);
        return notificationRepository.findAllByRecipientIdOrderByCreatedAtDescIdDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.getOrThrow(id);
        notification.markAsRead();
        return NotificationResponse.from(notification);
    }
}
