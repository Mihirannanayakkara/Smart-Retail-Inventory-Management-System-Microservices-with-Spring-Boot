package com.smartretail.supplier.repository;

import com.smartretail.supplier.entity.Restock;
import com.smartretail.supplier.entity.RestockStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RestockRepository extends MongoRepository<Restock, String> {
    List<Restock> findByStatus(RestockStatus status);
    List<Restock> findBySupplierId(String supplierId);
    List<Restock> findByProductId(String productId);
}
