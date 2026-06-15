package com.smartretail.supplier.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RestockRequestDto {

    @NotBlank(message = "Product ID (pid) is required")
    private String pid;              // ✅ REPLACES productId

    @NotBlank(message = "Supplier ID (sid) is required")
    private String sid;              // ✅ REPLACES supplierId

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private LocalDate expectedDate;
    private String createdBy;
    private String notes;
}