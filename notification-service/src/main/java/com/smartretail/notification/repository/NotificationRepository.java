package com.smartretail.notification.repository;

import com.smartretail.notification.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByType(String type);
    List<Notification> findByRecipient(String recipient);
}
