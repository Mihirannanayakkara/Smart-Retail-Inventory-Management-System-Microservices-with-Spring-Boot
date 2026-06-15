package com.smartretail.supplier.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "suppliers")
public class Supplier {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sid;          // ✅ NEW: custom supplier ID

    private String name;
    private String company;
    private String email;
    private String phone;
    private String address;
    private SupplierStatus status;
    private LocalDateTime createdAt;
}