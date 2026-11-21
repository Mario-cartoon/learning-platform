package com.example.learningplatform.notification.repository;

import com.example.learningplatform.common.exception.NotFoundException;
import com.example.learningplatform.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByRecipientIdOrderByCreatedAtDescIdDesc(Long recipientId);

    default Notification getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Уведомление", id));
    }
}
