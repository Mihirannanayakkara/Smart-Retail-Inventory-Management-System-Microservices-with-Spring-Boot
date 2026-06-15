package com.smartretail.user.messaging;

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

    public void publishUserRegistered(String userId, String name, String email) {
        EventPayload payload = EventPayload.builder()
                .eventType("USER_REGISTERED")
                .referenceId(userId)
                .recipient(email)
                .message("Welcome " + name + "! Your account has been created successfully.")
                .timestamp(LocalDateTime.now())
                .build();
        rabbitTemplate.convertAndSend(exchange, "user.registered", payload);
        log.info("Published USER_REGISTERED event for user: {}", userId);
    }
}
