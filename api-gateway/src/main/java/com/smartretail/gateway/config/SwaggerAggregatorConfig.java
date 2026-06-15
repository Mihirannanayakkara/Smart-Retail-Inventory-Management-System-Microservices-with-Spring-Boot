package com.smartretail.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Configuration
public class SwaggerAggregatorConfig {

    public record ServiceInfo(String name, String docsUrl, Map<String, String> pathRewrites) {}

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    public List<ServiceInfo> getServices() {
        return List.of(
                new ServiceInfo("User Service", "/aggregate/user/v3/api-docs",
                        Map.of("/api/v1/users", "/gateway/users")),
                new ServiceInfo("Product Service", "/aggregate/product/v3/api-docs",
                        Map.of("/api/v1/products", "/gateway/products")),
                new ServiceInfo("Order Service", "/aggregate/order/v3/api-docs",
                        Map.of("/api/v1/orders", "/gateway/orders")),
                new ServiceInfo("Supplier & Restock Service", "/aggregate/supplier/v3/api-docs",
                        Map.of("/api/v1/suppliers", "/gateway/suppliers",
                               "/api/v1/restocks", "/gateway/restocks",
                               "/api/v1/mappings", "/gateway/suppliers/mappings")),
                new ServiceInfo("Notification Service", "/aggregate/notification/v3/api-docs",
                        Map.of("/api/v1/notifications", "/gateway/notifications"))
        );
    }
}
