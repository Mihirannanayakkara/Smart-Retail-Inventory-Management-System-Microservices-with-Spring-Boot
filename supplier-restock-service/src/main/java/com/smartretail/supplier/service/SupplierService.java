package com.smartretail.supplier.service;

import com.smartretail.supplier.dto.SupplierRequest;
import com.smartretail.supplier.dto.SupplierResponse;
import com.smartretail.supplier.entity.Supplier;
import com.smartretail.supplier.entity.SupplierStatus;
import com.smartretail.supplier.exception.DuplicateResourceException;
import com.smartretail.supplier.exception.ResourceNotFoundException;
import com.smartretail.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierResponse createSupplier(SupplierRequest request) {
        // ✅ Check duplicate email
        if (supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Supplier with email " + request.getEmail() + " already exists");
        }
        // ✅ Check duplicate SID
        if (supplierRepository.existsBySid(request.getSid())) {
            throw new DuplicateResourceException("Supplier with SID " + request.getSid() + " already exists");
        }
        Supplier supplier = Supplier.builder()
                .sid(request.getSid())               // ✅ NEW
                .name(request.getName())
                .company(request.getCompany())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(SupplierStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        return mapToResponse(supplierRepository.save(supplier));
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SupplierResponse getSupplierById(String id) {
        return mapToResponse(findSupplierById(id));
    }

    // ✅ NEW: lookup by sid
    public SupplierResponse getSupplierBySid(String sid) {
        Supplier supplier = supplierRepository.findBySid(sid)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with sid: " + sid));
        return mapToResponse(supplier);
    }

    public SupplierResponse updateSupplier(String id, SupplierRequest request) {
        Supplier supplier = findSupplierById(id);
        if (!supplier.getEmail().equals(request.getEmail()) && supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }
        // ✅ Check duplicate SID only if sid changed
        if (!supplier.getSid().equals(request.getSid()) && supplierRepository.existsBySid(request.getSid())) {
            throw new DuplicateResourceException("Supplier with SID " + request.getSid() + " already exists");
        }
        supplier.setSid(request.getSid());           // ✅ NEW
        supplier.setName(request.getName());
        supplier.setCompany(request.getCompany());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        return mapToResponse(supplierRepository.save(supplier));
    }

    public void deleteSupplier(String id) {
        findSupplierById(id);
        supplierRepository.deleteById(id);
    }

    private Supplier findSupplierById(String id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .sid(supplier.getSid())              // ✅ NEW
                .name(supplier.getName())
                .company(supplier.getCompany())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .status(supplier.getStatus())
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}