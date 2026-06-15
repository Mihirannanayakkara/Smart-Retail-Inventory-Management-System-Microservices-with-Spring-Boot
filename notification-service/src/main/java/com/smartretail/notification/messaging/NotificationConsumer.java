package com.smartretail.notification.messaging;

import com.smartretail.notification.entity.Notification;
import com.smartretail.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = "${rabbitmq.queues.user-registered}")
    public void handleUserRegistered(EventPayload payload) {
        log.info("Received USER_REGISTERED event: {}", payload.getReferenceId());
        saveNotification(payload);
    }

    @RabbitListener(queues = "${rabbitmq.queues.order-created}")
    public void handleOrderCreated(EventPayload payload) {
        log.info("Received ORDER_CREATED event: {}", payload.getReferenceId());
        saveNotification(payload);
    }

    @RabbitListener(queues = "${rabbitmq.queues.order-cancelled}")
    public void handleOrderCancelled(EventPayload payload) {
        log.info("Received ORDER_CANCELLED event: {}", payload.getReferenceId());
        saveNotification(payload);
    }

    @RabbitListener(queues = "${rabbitmq.queues.low-stock}")
    public void handleLowStock(EventPayload payload) {
        log.info("Received LOW_STOCK event: {}", payload.getReferenceId());
        saveNotification(payload);
    }

    @RabbitListener(queues = "${rabbitmq.queues.restock-created}")
    public void handleRestockCreated(EventPayload payload) {
        log.info("Received RESTOCK_CREATED event: {}", payload.getReferenceId());
        saveNotification(payload);
    }

    @RabbitListener(queues = "${rabbitmq.queues.restock-completed}")
    public void handleRestockCompleted(EventPayload payload) {
        log.info("Received RESTOCK_COMPLETED event: {}", payload.getReferenceId());
        saveNotification(payload);
    }

    private void saveNotification(EventPayload payload) {
        Notification notification = Notification.builder()
                .type(payload.getEventType())
                .message(payload.getMessage())
                .referenceId(payload.getReferenceId())
                .recipient(payload.getRecipient())
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }
}
