package com.smartretail.supplier.controller;

import com.smartretail.supplier.dto.RestockRequestDto;
import com.smartretail.supplier.dto.RestockResponse;
import com.smartretail.supplier.entity.RestockStatus;
import com.smartretail.supplier.service.RestockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restocks")
@RequiredArgsConstructor
@Tag(name = "Restock Management", description = "APIs for managing restock requests")
public class RestockController {

    private final RestockService restockService;

    @PostMapping
    @Operation(summary = "Create a new restock request")
    public ResponseEntity<RestockResponse> createRestock(@Valid @RequestBody RestockRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restockService.createRestock(request));
    }

    @GetMapping
    @Operation(summary = "Get all restock requests")
    public ResponseEntity<List<RestockResponse>> getAllRestocks() {
        return ResponseEntity.ok(restockService.getAllRestocks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restock request by ID")
    public ResponseEntity<RestockResponse> getRestockById(@PathVariable String id) {
        return ResponseEntity.ok(restockService.getRestockById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update restock request")
    public ResponseEntity<RestockResponse> updateRestock(@PathVariable String id,
                                                          @Valid @RequestBody RestockRequestDto request) {
        return ResponseEntity.ok(restockService.updateRestock(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete restock request")
    public ResponseEntity<Void> deleteRestock(@PathVariable String id) {
        restockService.deleteRestock(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a restock request")
    public ResponseEntity<RestockResponse> approveRestock(@PathVariable String id) {
        return ResponseEntity.ok(restockService.approveRestock(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a restock request")
    public ResponseEntity<RestockResponse> cancelRestock(@PathVariable String id) {
        return ResponseEntity.ok(restockService.cancelRestock(id));
    }

    @PutMapping("/{id}/mark-delivered")
    @Operation(summary = "Mark restock as delivered and update product stock")
    public ResponseEntity<RestockResponse> markDelivered(@PathVariable String id) {
        return ResponseEntity.ok(restockService.markDelivered(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get restock requests by status")
    public ResponseEntity<List<RestockResponse>> getRestocksByStatus(@PathVariable RestockStatus status) {
        return ResponseEntity.ok(restockService.getRestocksByStatus(status));
    }
}
