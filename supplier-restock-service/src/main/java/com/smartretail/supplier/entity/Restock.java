package com.smartretail.supplier.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "restock_requests")
public class Restock {

    @Id
    private String id;

    private String productId;       // stores pid ✅

    private String supplierId;      // stores MongoDB id (internal use only)

    private String supplierSid;     // ✅ NEW: stores sid for display

    private Integer quantity;

    private LocalDate requestDate;

    private LocalDate expectedDate;

    private RestockStatus status;

    private String createdBy;

    private String notes;

    private LocalDateTime createdAt;
}