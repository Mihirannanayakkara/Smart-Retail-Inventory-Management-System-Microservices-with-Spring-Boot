package com.smartretail.product.service;

import com.smartretail.product.dto.ProductRequest;
import com.smartretail.product.dto.ProductResponse;
import com.smartretail.product.dto.StockUpdateRequest;
import com.smartretail.product.entity.Product;
import com.smartretail.product.exception.DuplicateResourceException;
import com.smartretail.product.exception.InsufficientStockException;
import com.smartretail.product.exception.ResourceNotFoundException;
import com.smartretail.product.messaging.EventPublisher;
import com.smartretail.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;

    public ProductResponse createProduct(ProductRequest request) {
        // ✅ Check duplicate SKU
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product with SKU " + request.getSku() + " already exists");
        }
        // ✅ Check duplicate PID
        if (productRepository.existsByPid(request.getPid())) {
            throw new DuplicateResourceException("Product with PID " + request.getPid() + " already exists");
        }
        Product product = Product.builder()
                .pid(request.getPid())               // ✅ NEW
                .name(request.getName())
                .sku(request.getSku())
                .category(request.getCategory())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .lowStockThreshold(request.getLowStockThreshold())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .build();
        return mapToResponse(productRepository.save(product));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(String id) {
        return mapToResponse(findProductById(id));
    }

    // ✅ NEW: lookup by pid
    public ProductResponse getProductByPid(String pid) {
        Product product = productRepository.findByPid(pid)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with pid: " + pid));
        return mapToResponse(product);
    }

    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = findProductById(id);
        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product with SKU " + request.getSku() + " already exists");
        }
        // ✅ Check duplicate PID only if pid changed
        if (!product.getPid().equals(request.getPid()) && productRepository.existsByPid(request.getPid())) {
            throw new DuplicateResourceException("Product with PID " + request.getPid() + " already exists");
        }
        product.setPid(request.getPid());            // ✅ NEW
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setDescription(request.getDescription());
        return mapToResponse(productRepository.save(product));
    }

    public void deleteProduct(String id) {
        findProductById(id);
        productRepository.deleteById(id);
    }

    public ProductResponse increaseStock(String id, StockUpdateRequest request) {
        Product product = findProductById(id);
        product.setQuantity(product.getQuantity() + request.getQuantity());
        return mapToResponse(productRepository.save(product));
    }

    public ProductResponse decreaseStock(String id, StockUpdateRequest request) {
        Product product = findProductById(id);
        if (product.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + product.getName() +
                    ". Available: " + product.getQuantity() + ", Requested: " + request.getQuantity());
        }
        product.setQuantity(product.getQuantity() - request.getQuantity());
        Product saved = productRepository.save(product);
        if (saved.getQuantity() <= saved.getLowStockThreshold()) {
            eventPublisher.publishLowStock(saved.getId(), saved.getName(), saved.getQuantity());
        }
        return mapToResponse(saved);
    }

    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Product findProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .pid(product.getPid())               // ✅ NEW
                .name(product.getName())
                .sku(product.getSku())
                .category(product.getCategory())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .lowStockThreshold(product.getLowStockThreshold())
                .description(product.getDescription())
                .lowStock(product.getQuantity() <= product.getLowStockThreshold())
                .createdAt(product.getCreatedAt())
                .build();
    }
}