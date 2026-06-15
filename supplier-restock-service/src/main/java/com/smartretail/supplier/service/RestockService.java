package com.smartretail.supplier.service;

import com.smartretail.supplier.client.ProductClient;
import com.smartretail.supplier.dto.RestockRequestDto;
import com.smartretail.supplier.dto.RestockResponse;
import com.smartretail.supplier.entity.Restock;
import com.smartretail.supplier.entity.RestockStatus;
import com.smartretail.supplier.entity.Supplier;
import com.smartretail.supplier.exception.ResourceNotFoundException;
import com.smartretail.supplier.messaging.EventPublisher;
import com.smartretail.supplier.repository.RestockRepository;
import com.smartretail.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestockService {

    private final RestockRepository restockRepository;
    private final SupplierRepository supplierRepository;
    private final ProductClient productClient;
    private final EventPublisher eventPublisher;

    public RestockResponse createRestock(RestockRequestDto request) {
        // ✅ Lookup supplier by sid
        Supplier supplier = supplierRepository.findBySid(request.getSid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with sid: " + request.getSid()));

        Restock restock = Restock.builder()
                .productId(request.getPid())             // ✅ store pid
                .supplierId(supplier.getId())            // ✅ store MongoDB id internally
                .supplierSid(supplier.getSid())          // ✅ store sid for display
                .quantity(request.getQuantity())
                .requestDate(LocalDate.now())
                .expectedDate(request.getExpectedDate())
                .status(RestockStatus.PENDING)
                .createdBy(request.getCreatedBy())
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();

        Restock saved = restockRepository.save(restock);
        eventPublisher.publishRestockCreated(saved.getId(), saved.getProductId(), saved.getQuantity());
        return mapToResponse(saved);
    }

    public List<RestockResponse> getAllRestocks() {
        return restockRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RestockResponse getRestockById(String id) {
        return mapToResponse(findRestockById(id));
    }

    public RestockResponse updateRestock(String id, RestockRequestDto request) {
        Restock restock = findRestockById(id);
        if (restock.getStatus() != RestockStatus.PENDING) {
            throw new IllegalStateException("Only PENDING restocks can be updated");
        }
        // ✅ Lookup supplier by sid
        Supplier supplier = supplierRepository.findBySid(request.getSid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier not found with sid: " + request.getSid()));

        restock.setProductId(request.getPid());          // ✅ store pid
        restock.setSupplierId(supplier.getId());         // ✅ store MongoDB id internally
        restock.setSupplierSid(supplier.getSid());       // ✅ store sid for display
        restock.setQuantity(request.getQuantity());
        restock.setExpectedDate(request.getExpectedDate());
        restock.setNotes(request.getNotes());
        return mapToResponse(restockRepository.save(restock));
    }

    public void deleteRestock(String id) {
        findRestockById(id);
        restockRepository.deleteById(id);
    }

    public RestockResponse approveRestock(String id) {
        Restock restock = findRestockById(id);
        if (restock.getStatus() != RestockStatus.PENDING) {
            throw new IllegalStateException("Only PENDING restocks can be approved");
        }
        restock.setStatus(RestockStatus.APPROVED);
        return mapToResponse(restockRepository.save(restock));
    }

    public RestockResponse cancelRestock(String id) {
        Restock restock = findRestockById(id);
        if (restock.getStatus() == RestockStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a delivered restock");
        }
        restock.setStatus(RestockStatus.CANCELLED);
        return mapToResponse(restockRepository.save(restock));
    }

    public RestockResponse markDelivered(String id) {
        Restock restock = findRestockById(id);
        if (restock.getStatus() != RestockStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED restocks can be marked as delivered");
        }
        try {
            productClient.increaseStock(restock.getProductId(), Map.of("quantity", restock.getQuantity()));
        } catch (Exception e) {
            log.warn("Could not update product stock for product: {}. Error: {}",
                    restock.getProductId(), e.getMessage());
        }
        restock.setStatus(RestockStatus.DELIVERED);
        Restock saved = restockRepository.save(restock);
        eventPublisher.publishRestockCompleted(saved.getId(), saved.getProductId(), saved.getQuantity());
        return mapToResponse(saved);
    }

    public List<RestockResponse> getRestocksByStatus(RestockStatus status) {
        return restockRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Restock findRestockById(String id) {
        return restockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restock request not found with id: " + id));
    }

    private RestockResponse mapToResponse(Restock restock) {
        return RestockResponse.builder()
                .id(restock.getId())
                .productId(restock.getProductId())       // holds pid
                .supplierId(restock.getSupplierId())     // holds MongoDB id
                .supplierSid(restock.getSupplierSid())   // ✅ holds sid for display
                .quantity(restock.getQuantity())
                .requestDate(restock.getRequestDate())
                .expectedDate(restock.getExpectedDate())
                .status(restock.getStatus())
                .createdBy(restock.getCreatedBy())
                .notes(restock.getNotes())
                .createdAt(restock.getCreatedAt())
                .build();
    }
}