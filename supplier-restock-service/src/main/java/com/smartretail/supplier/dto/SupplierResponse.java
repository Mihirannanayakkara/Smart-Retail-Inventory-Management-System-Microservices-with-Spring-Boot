package com.smartretail.supplier.dto;

import com.smartretail.supplier.entity.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private String id;
    private String sid;          // ✅ NEW
    private String name;
    private String company;
    private String email;
    private String phone;
    private String address;
    private SupplierStatus status;
    private LocalDateTime createdAt;
}