package com.smartretail.supplier.messaging;

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

    public void publishRestockCreated(String restockId, String productId, Integer quantity) {
        EventPayload payload = EventPayload.builder()
                .eventType("RESTOCK_CREATED")
                .referenceId(restockId)
                .recipient("admin")
                .message("Restock request #" + restockId + " created for product " + productId + ". Quantity: " + quantity)
                .timestamp(LocalDateTime.now())
                .build();
        rabbitTemplate.convertAndSend(exchange, "restock.created", payload);
        log.info("Published RESTOCK_CREATED event for restock: {}", restockId);
    }

    public void publishRestockCompleted(String restockId, String productId, Integer quantity) {
        EventPayload payload = EventPayload.builder()
                .eventType("RESTOCK_COMPLETED")
                .referenceId(restockId)
                .recipient("admin")
                .message("Restock #" + restockId + " delivered. Stock increased by " + quantity + " for product " + productId)
                .timestamp(LocalDateTime.now())
                .build();
        rabbitTemplate.convertAndSend(exchange, "restock.completed", payload);
        log.info("Published RESTOCK_COMPLETED event for restock: {}", restockId);
    }
}
