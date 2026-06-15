package com.smartretail.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Smart Retail System - Order Management Service")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:9093")  // ← order service's own port
                                .description("Order Service - Direct"),
                        new Server()
                                .url("http://localhost:9090/gateway")  // ← optional gateway
                                .description("API Gateway")
                ));
    }
}