package com.smartretail.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MappingRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Supplier ID is required")
    private String supplierId;

    @NotNull(message = "Unit cost is required")
    private BigDecimal unitCost;

    private Integer leadTimeDays;

    private boolean preferredSupplier;
}
