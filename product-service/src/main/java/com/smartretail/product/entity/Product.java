package com.smartretail.product.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    private String pid;          // ✅ NEW: custom product ID

    private String name;

    @Indexed(unique = true)
    private String sku;

    private String category;
    private BigDecimal price;
    private Integer quantity;
    private Integer lowStockThreshold;
    private String description;
    private LocalDateTime createdAt;
}