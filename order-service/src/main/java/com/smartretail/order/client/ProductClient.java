package com.smartretail.order.client;

import com.smartretail.order.dto.ProductDto;
import com.smartretail.order.dto.StockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ProductDto getProductById(@PathVariable("id") String id);

    @PutMapping("/api/v1/products/{id}/decrease-stock")
    ProductDto decreaseStock(@PathVariable("id") String id, @RequestBody StockUpdateRequest request);

    @PutMapping("/api/v1/products/{id}/increase-stock")
    ProductDto increaseStock(@PathVariable("id") String id, @RequestBody StockUpdateRequest request);
}
