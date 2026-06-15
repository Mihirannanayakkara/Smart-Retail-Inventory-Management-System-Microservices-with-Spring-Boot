package com.smartretail.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("Smart Retail System - Notification Service (RabbitMQ Consumer)")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:9095")  // ← notification service's own port
                                .description("Notification Service - Direct"),
                        new Server()
                                .url("http://localhost:9090/gateway")  // ← optional gateway
                                .description("API Gateway")
                ));
    }
}