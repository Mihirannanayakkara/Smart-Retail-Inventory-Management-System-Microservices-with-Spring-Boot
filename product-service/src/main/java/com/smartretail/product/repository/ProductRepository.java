package com.smartretail.product.repository;

import com.smartretail.product.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    Optional<Product> findByPid(String pid);       // ✅ NEW
    boolean existsByPid(String pid);               // ✅ NEW
    List<Product> findByCategoryIgnoreCase(String category);
    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("{ '$expr': { '$lte': ['$quantity', '$lowStockThreshold'] } }")
    List<Product> findLowStockProducts();
}