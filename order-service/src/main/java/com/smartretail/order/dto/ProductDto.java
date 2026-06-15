package com.smartretail.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private String id;
    private String name;
    private String sku;
    private String category;
    private BigDecimal price;
    private Integer quantity;
    private Integer lowStockThreshold;
    private boolean lowStock;
}
