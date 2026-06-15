package com.smartretail.order.service;

import com.smartretail.order.client.ProductClient;
import com.smartretail.order.client.UserClient;
import com.smartretail.order.dto.*;
import com.smartretail.order.entity.Order;
import com.smartretail.order.entity.OrderItem;
import com.smartretail.order.entity.OrderStatus;
import com.smartretail.order.exception.OrderProcessingException;
import com.smartretail.order.exception.ResourceNotFoundException;
import com.smartretail.order.messaging.EventPublisher;
import com.smartretail.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final EventPublisher eventPublisher;

    public OrderResponse createOrder(OrderRequest request) {
        UserDto user = validateCustomer(request.getUserId(), request.getEmail());
        List<OrderItemRequest> normalizedItems = normalizeItems(request.getItems());
        Map<String, ProductDto> products = fetchProducts(normalizedItems);

        validateCreateStock(normalizedItems, products);
        applyCreateStockChanges(normalizedItems);

        List<OrderItem> orderItems = buildOrderItems(normalizedItems, products);
        BigDecimal subTotal = calculateSubTotal(orderItems);

        Order order = Order.builder()
                .userId(user.getId())
                .customerName(user.getName())
                .customerEmail(request.getEmail().trim())
                .shippingAddress(request.getShippingAddress().trim())
                .paymentMethod(request.getPaymentMethod().trim())
                .notes(trimToNull(request.getNotes()))
                .status(OrderStatus.PLACED)
                .subTotal(subTotal)
                .shippingFee(ZERO)
                .taxAmount(ZERO)
                .totalAmount(subTotal)
                .items(orderItems)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderCreated(saved.getId(), saved.getUserId(), saved.getTotalAmount().toString());
        return mapToResponse(saved);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(String id) {
        return mapToResponse(findOrderById(id));
    }

    public OrderResponse updateOrder(String id, OrderRequest request) {
        Order order = findOrderById(id);
        ensurePlaced(order, "Only PLACED orders can be edited");

        UserDto user = validateCustomer(request.getUserId(), request.getEmail());
        List<OrderItemRequest> normalizedItems = normalizeItems(request.getItems());
        Map<String, ProductDto> products = fetchProducts(normalizedItems);

        applyUpdateStockChanges(order, normalizedItems, products);

        List<OrderItem> rebuiltItems = buildOrderItems(normalizedItems, products);
        BigDecimal subTotal = calculateSubTotal(rebuiltItems);

        order.setUserId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerEmail(request.getEmail().trim());
        order.setShippingAddress(request.getShippingAddress().trim());
        order.setPaymentMethod(request.getPaymentMethod().trim());
        order.setNotes(trimToNull(request.getNotes()));
        order.setItems(rebuiltItems);
        order.setSubTotal(subTotal);
        order.setShippingFee(ZERO);
        order.setTaxAmount(ZERO);
        order.setTotalAmount(subTotal);
        order.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(orderRepository.save(order));
    }

    public OrderResponse cancelOrder(String id) {
        Order order = findOrderById(id);
        ensurePlaced(order, "Only PLACED orders can be cancelled");

        for (OrderItem item : order.getItems()) {
            try {
                productClient.increaseStock(item.getProductId(), stockUpdateRequest(item.getQuantity()));
            } catch (Exception e) {
                log.warn("Could not restore stock for product {} while cancelling order {}", item.getProductId(), order.getId());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderCancelled(saved.getId(), saved.getUserId());
        return mapToResponse(saved);
    }

    public OrderResponse completeOrder(String id) {
        Order order = findOrderById(id);
        ensurePlaced(order, "Only PLACED orders can be completed");
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(orderRepository.save(order));
    }

    public List<OrderResponse> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserDto validateCustomer(String userId, String email) {
        UserDto user;
        try {
            user = userClient.getUserById(userId);
        } catch (Exception e) {
            throw new OrderProcessingException("Customer not found or user service unavailable: " + userId);
        }

        if (user == null) {
            throw new OrderProcessingException("Customer not found: " + userId);
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new OrderProcessingException("Customer account is not active");
        }
        if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email == null ? "" : email.trim())) {
            throw new OrderProcessingException("Entered email does not match the selected customer ID");
        }
        return user;
    }

    private List<OrderItemRequest> normalizeItems(List<OrderItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new OrderProcessingException("Order must have at least one item");
        }

        LinkedHashMap<String, Integer> merged = new LinkedHashMap<>();
        for (OrderItemRequest request : requests) {
            String productId = request.getProductId() == null ? "" : request.getProductId().trim();
            Integer quantity = request.getQuantity();

            if (productId.isBlank()) {
                throw new OrderProcessingException("Product ID is required for every order item");
            }
            if (quantity == null || quantity < 1) {
                throw new OrderProcessingException("Quantity must be at least 1 for every order item");
            }

            merged.merge(productId, quantity, Integer::sum);
        }

        return merged.entrySet().stream()
                .map(entry -> {
                    OrderItemRequest item = new OrderItemRequest();
                    item.setProductId(entry.getKey());
                    item.setQuantity(entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private Map<String, ProductDto> fetchProducts(List<OrderItemRequest> items) {
        Map<String, ProductDto> products = new HashMap<>();
        for (OrderItemRequest item : items) {
            products.computeIfAbsent(item.getProductId(), this::getProductOrThrow);
        }
        return products;
    }

    private ProductDto getProductOrThrow(String productId) {
        try {
            ProductDto product = productClient.getProductById(productId);
            if (product == null) {
                throw new OrderProcessingException("Product not found: " + productId);
            }
            return product;
        } catch (OrderProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException("Product not found: " + productId);
        }
    }

    private void validateCreateStock(List<OrderItemRequest> items, Map<String, ProductDto> products) {
        for (OrderItemRequest item : items) {
            ProductDto product = products.get(item.getProductId());
            if (product.getQuantity() < item.getQuantity()) {
                throw new OrderProcessingException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getQuantity() + ", Requested: " + item.getQuantity());
            }
        }
    }

    private void applyCreateStockChanges(List<OrderItemRequest> items) {
        for (OrderItemRequest item : items) {
            productClient.decreaseStock(item.getProductId(), stockUpdateRequest(item.getQuantity()));
        }
    }

    private void applyUpdateStockChanges(Order existingOrder,
                                         List<OrderItemRequest> newItems,
                                         Map<String, ProductDto> newProducts) {
        Map<String, Integer> oldQuantities = new HashMap<>();
        for (OrderItem item : existingOrder.getItems()) {
            oldQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        Map<String, Integer> newQuantities = new HashMap<>();
        for (OrderItemRequest item : newItems) {
            newQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        Set<String> productIds = new HashSet<>();
        productIds.addAll(oldQuantities.keySet());
        productIds.addAll(newQuantities.keySet());

        Map<String, ProductDto> allProducts = new HashMap<>(newProducts);
        for (String productId : productIds) {
            allProducts.computeIfAbsent(productId, this::getProductOrThrow);
        }

        for (String productId : productIds) {
            int oldQty = oldQuantities.getOrDefault(productId, 0);
            int newQty = newQuantities.getOrDefault(productId, 0);
            int delta = newQty - oldQty;

            if (delta > 0) {
                ProductDto product = allProducts.get(productId);
                if (product.getQuantity() < delta) {
                    throw new OrderProcessingException(
                            "Insufficient stock for product: " + product.getName() +
                                    ". Available: " + product.getQuantity() + ", Additional requested: " + delta);
                }
            }
        }

        for (String productId : productIds) {
            int oldQty = oldQuantities.getOrDefault(productId, 0);
            int newQty = newQuantities.getOrDefault(productId, 0);
            int delta = newQty - oldQty;

            if (delta > 0) {
                productClient.decreaseStock(productId, stockUpdateRequest(delta));
            } else if (delta < 0) {
                productClient.increaseStock(productId, stockUpdateRequest(Math.abs(delta)));
            }
        }
    }

    private List<OrderItem> buildOrderItems(List<OrderItemRequest> items, Map<String, ProductDto> products) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest request : items) {
            ProductDto product = products.get(request.getProductId());
            BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            orderItems.add(OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .subTotal(subTotal)
                    .build());
        }

        return orderItems;
    }

    private BigDecimal calculateSubTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private StockUpdateRequest stockUpdateRequest(int quantity) {
        StockUpdateRequest request = new StockUpdateRequest();
        request.setQuantity(quantity);
        return request;
    }

    private void ensurePlaced(Order order, String message) {
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new OrderProcessingException(message);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Order findOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subTotal(item.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .status(order.getStatus())
                .subTotal(order.getSubTotal())
                .shippingFee(order.getShippingFee())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
