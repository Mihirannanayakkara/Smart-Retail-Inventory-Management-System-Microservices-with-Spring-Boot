package com.smartretail.order.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPayload {
    private String eventType;
    private String referenceId;
    private String recipient;
    private String message;
    private LocalDateTime timestamp;
}
