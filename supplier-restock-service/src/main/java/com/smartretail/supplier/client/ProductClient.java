package com.smartretail.supplier.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "product-service")
public interface ProductClient {

    @PutMapping("/products/{id}/increase-stock")
    Object increaseStock(@PathVariable("id") String id, @RequestBody Map<String, Integer> request);
}
