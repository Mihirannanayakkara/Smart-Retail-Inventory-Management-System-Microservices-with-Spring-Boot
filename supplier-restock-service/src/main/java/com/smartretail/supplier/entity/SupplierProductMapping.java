package com.smartretail.supplier.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "supplier_product_mappings")
public class SupplierProductMapping {

    @Id
    private String id;

    private String productId;

    private String supplierId;

    private BigDecimal unitCost;

    private Integer leadTimeDays;

    private boolean preferredSupplier;
}
