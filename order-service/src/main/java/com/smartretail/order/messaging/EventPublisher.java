package com.smartretail.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    public void publishOrderCreated(String orderId, String userId, String totalAmount) {
        EventPayload payload = EventPayload.builder()
                .eventType("ORDER_CREATED")
                .referenceId(orderId)
                .recipient(userId)
                .message("Order #" + orderId + " has been created successfully. Total: " + totalAmount)
                .timestamp(LocalDateTime.now())
                .build();
        rabbitTemplate.convertAndSend(exchange, "order.created", payload);
        log.info("Published ORDER_CREATED event for order: {}", orderId);
    }

    public void publishOrderCancelled(String orderId, String userId) {
        EventPayload payload = EventPayload.builder()
                .eventType("ORDER_CANCELLED")
                .referenceId(orderId)
                .recipient(userId)
                .message("Order #" + orderId + " has been cancelled.")
                .timestamp(LocalDateTime.now())
                .build();
        rabbitTemplate.convertAndSend(exchange, "order.cancelled", payload);
        log.info("Published ORDER_CANCELLED event for order: {}", orderId);
    }
}
