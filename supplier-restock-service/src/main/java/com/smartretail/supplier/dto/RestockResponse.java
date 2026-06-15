package com.smartretail.supplier.dto;

import com.smartretail.supplier.entity.RestockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestockResponse {
    private String id;
    private String productId;       // holds pid
    private String supplierId;      // holds MongoDB id (internal)
    private String supplierSid;     // ✅ NEW: holds sid for display
    private Integer quantity;
    private LocalDate requestDate;
    private LocalDate expectedDate;
    private RestockStatus status;
    private String createdBy;
    private String notes;
    private LocalDateTime createdAt;
}