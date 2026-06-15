package com.smartretail.supplier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingResponse {
    private String id;
    private String productId;
    private String supplierId;
    private BigDecimal unitCost;
    private Integer leadTimeDays;
    private boolean preferredSupplier;
}
