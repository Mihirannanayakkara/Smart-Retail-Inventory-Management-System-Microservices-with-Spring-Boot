package com.smartretail.product.messaging;

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

    public void publishLowStock(String productId, String productName, int currentQuantity) {
        EventPayload payload = EventPayload.builder()
                .eventType("LOW_STOCK")
                .referenceId(productId)
                .recipient("admin")
                .message("Low stock alert: Product '" + productName + "' has only " + currentQuantity + " units left.")
                .timestamp(LocalDateTime.now())
                .build();
        rabbitTemplate.convertAndSend(exchange, "product.low.stock", payload);
        log.info("Published LOW_STOCK event for product: {}", productId);
    }
}
