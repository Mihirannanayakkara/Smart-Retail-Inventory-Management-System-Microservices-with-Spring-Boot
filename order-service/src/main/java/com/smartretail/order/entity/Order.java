package com.smartretail.order.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    private String userId;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private String paymentMethod;
    private String notes;

    private OrderStatus status;

    private BigDecimal subTotal;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private List<OrderItem> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
