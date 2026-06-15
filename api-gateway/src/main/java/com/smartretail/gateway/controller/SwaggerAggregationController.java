package com.smartretail.gateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartretail.gateway.config.SwaggerAggregatorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Configuration
public class SwaggerAggregationController {

    @Bean
    public WebFilter swaggerWebFilter(WebClient.Builder webClientBuilder,
                                      SwaggerAggregatorConfig aggregatorConfig,
                                      ObjectMapper objectMapper) {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            HttpMethod method = request.getMethod();

            if ("/swagger-ui.html".equals(path) && method == HttpMethod.GET) {
                return serveSwaggerUI(exchange);
            }

            if ("/aggregate/v3/api-docs".equals(path) && method == HttpMethod.GET) {
                return serveAggregatedApiDocs(exchange, webClientBuilder, aggregatorConfig, objectMapper);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> serveSwaggerUI(ServerWebExchange exchange) {
        try {
            ClassPathResource resource = new ClassPathResource("static/swagger-ui.html");
            byte[] content = resource.getInputStream().readAllBytes();
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.TEXT_HTML);
            response.getHeaders().setContentLength(content.length);
            DataBufferFactory bufferFactory = response.bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(content);
            return response.writeWith(Mono.just(buffer));
        } catch (IOException e) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    private Mono<Void> serveAggregatedApiDocs(ServerWebExchange exchange,
                                               WebClient.Builder webClientBuilder,
                                               SwaggerAggregatorConfig aggregatorConfig,
                                               ObjectMapper objectMapper) {
        return Flux.fromIterable(aggregatorConfig.getServices())
                .flatMap(service -> webClientBuilder.build()
                        .get()
                        .uri("http://localhost:9090" + service.docsUrl())
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .onErrorResume(e -> Mono.just(objectMapper.createObjectNode()))
                        .map(spec -> Map.entry(service, spec)))
                .collectList()
                .map(entries -> buildUnifiedSpec(entries, objectMapper))
                .flatMap(spec -> {
                    try {
                        byte[] content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(spec);
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.OK);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        response.getHeaders().setContentLength(content.length);
                        DataBufferFactory bufferFactory = response.bufferFactory();
                        DataBuffer buffer = bufferFactory.wrap(content);
                        return response.writeWith(Mono.just(buffer));
                    } catch (Exception e) {
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return response.setComplete();
                    }
                });
    }

    private ObjectNode buildUnifiedSpec(java.util.List<Map.Entry<SwaggerAggregatorConfig.ServiceInfo, JsonNode>> entries,
                                         ObjectMapper mapper) {
        ObjectNode unified = mapper.createObjectNode();
        unified.put("openapi", "3.0.1");

        ObjectNode info = mapper.createObjectNode();
        info.put("title", "Smart Retail System - Unified API");
        info.put("description", "API Gateway - All Microservices Endpoints");
        info.put("version", "1.0.0");
        unified.set("info", info);

        ArrayNode servers = mapper.createArrayNode();
        ObjectNode server = mapper.createObjectNode();
        server.put("url", "http://localhost:9090");
        server.put("description", "API Gateway");
        servers.add(server);
        unified.set("servers", servers);

        ObjectNode paths = mapper.createObjectNode();
        ObjectNode components = mapper.createObjectNode();
        ObjectNode schemas = mapper.createObjectNode();

        for (var entry : entries) {
            SwaggerAggregatorConfig.ServiceInfo service = entry.getKey();
            JsonNode spec = entry.getValue();
            String prefix = getServicePrefix(service.name());

            if (spec.has("components") && spec.get("components").has("schemas")) {
                JsonNode serviceSchemas = spec.get("components").get("schemas");
                Iterator<Map.Entry<String, JsonNode>> schemaFields = serviceSchemas.fields();
                while (schemaFields.hasNext()) {
                    Map.Entry<String, JsonNode> schemaEntry = schemaFields.next();
                    String originalName = schemaEntry.getKey();
                    String prefixedName = prefix + "_" + originalName;
                    if (!schemas.has(prefixedName)) {
                        JsonNode schema = schemaEntry.getValue().deepCopy();
                        updateRefsInNode(schema, prefix);
                        schemas.set(prefixedName, schema);
                    }
                }
            }
        }

        for (var entry : entries) {
            SwaggerAggregatorConfig.ServiceInfo service = entry.getKey();
            JsonNode spec = entry.getValue();
            String prefix = getServicePrefix(service.name());

            if (spec.has("paths")) {
                Iterator<Map.Entry<String, JsonNode>> pathFields = spec.get("paths").fields();
                while (pathFields.hasNext()) {
                    Map.Entry<String, JsonNode> pathEntry = pathFields.next();
                    String originalPath = pathEntry.getKey();
                    String gatewayPath = rewritePath(originalPath, service.pathRewrites());

                    if (gatewayPath == null) {
                        continue;
                    }

                    JsonNode pathNode = pathEntry.getValue().deepCopy();
                    updateRefsInNode(pathNode, prefix);

                    Iterator<Map.Entry<String, JsonNode>> opFields = ((ObjectNode) pathNode).fields();
                    while (opFields.hasNext()) {
                        Map.Entry<String, JsonNode> opEntry = opFields.next();
                        String method = opEntry.getKey();
                        if (Set.of("get", "post", "put", "delete", "patch").contains(method)) {
                            ObjectNode operation = (ObjectNode) opEntry.getValue();
                            ArrayNode tags = mapper.createArrayNode();
                            tags.add(service.name());
                            operation.set("tags", tags);
                        }
                    }

                    paths.set(gatewayPath, pathNode);
                }
            }
        }

        components.set("schemas", schemas);
        unified.set("paths", paths);
        unified.set("components", components);

        return unified;
    }

    private String getServicePrefix(String serviceName) {
        return serviceName.replaceAll("\\s+", "").replace("&", "");
    }

    private void updateRefsInNode(JsonNode node, String prefix) {
        if (node == null) return;

        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                if ("$ref".equals(key) && value.isTextual()) {
                    String ref = value.textValue();
                    if (ref.contains("/schemas/")) {
                        String schemaName = ref.substring(ref.lastIndexOf("/schemas/") + 9);
                        objNode.put(key, "#/components/schemas/" + prefix + "_" + schemaName);
                    }
                } else {
                    updateRefsInNode(value, prefix);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                updateRefsInNode(element, prefix);
            }
        }
    }

    private String rewritePath(String originalPath, Map<String, String> pathRewrites) {
        for (Map.Entry<String, String> entry : pathRewrites.entrySet()) {
            String apiPrefix = entry.getKey();
            String gatewayPrefix = entry.getValue();
            if (originalPath.startsWith(apiPrefix)) {
                return gatewayPrefix + originalPath.substring(apiPrefix.length());
            }
        }
        return null;
    }
}
