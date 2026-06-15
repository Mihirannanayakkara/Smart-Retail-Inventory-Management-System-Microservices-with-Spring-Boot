package com.smartretail.supplier.repository;

import com.smartretail.supplier.entity.Supplier;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface SupplierRepository extends MongoRepository<Supplier, String> {
    boolean existsByEmail(String email);
    Optional<Supplier> findBySid(String sid);      // ✅ NEW
    boolean existsBySid(String sid);               // ✅ NEW
}