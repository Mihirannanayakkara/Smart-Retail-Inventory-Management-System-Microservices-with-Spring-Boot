package com.smartretail.supplier.repository;

import com.smartretail.supplier.entity.SupplierProductMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SupplierProductMappingRepository extends MongoRepository<SupplierProductMapping, String> {
    List<SupplierProductMapping> findByProductId(String productId);
    List<SupplierProductMapping> findBySupplierId(String supplierId);
    boolean existsByProductIdAndSupplierId(String productId, String supplierId);
}
