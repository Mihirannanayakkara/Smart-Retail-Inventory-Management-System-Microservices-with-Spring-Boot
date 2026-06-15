package com.smartretail.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String id;
    private String pid;          // ✅ NEW
    private String name;
    private String sku;
    private String category;
    private BigDecimal price;
    private Integer quantity;
    private Integer lowStockThreshold;
    private String description;
    private boolean lowStock;
    private LocalDateTime createdAt;
}