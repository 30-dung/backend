package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.ServiceRequest;
import com.example.serversideclinet.model.ServiceEntity;
import com.example.serversideclinet.model.StoreService; // Correct import
import com.example.serversideclinet.service.ServiceService;
import com.example.serversideclinet.service.StoreServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/services")
public class ServiceController {

    @Autowired
    private StoreServiceService storeServiceService;

    @Autowired
    private ServiceService serviceService;

    // Admin mới được phép tạo service
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createService(@RequestBody ServiceRequest request) {
        ServiceEntity service = serviceService.create(request);
        return ResponseEntity.ok(service);
    }

    // Cả admin và customer đều có thể lấy danh sách dịch vụ
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping
    public ResponseEntity<?> getAllServices() {
        return ResponseEntity.ok(serviceService.getAll());
    }

    // Cả admin và customer đều có thể lấy service theo id
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceById(@PathVariable Integer id) {
        return serviceService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Chỉ admin mới được cập nhật dịch vụ
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(@PathVariable Integer id, @RequestBody ServiceRequest request) {
        return serviceService.update(id, request)
                .map(updated -> ResponseEntity.ok("Updated successfully"))
                .orElse(ResponseEntity.notFound().build());
    }

    // Chỉ admin mới được xóa dịch vụ
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Integer id) {
        if (serviceService.delete(id)) {
            return ResponseEntity.ok("Deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<StoreService>> getServicesByStore(@PathVariable Integer storeId) {
        List<StoreService> services = storeServiceService.getServicesByStoreId(storeId);
        return ResponseEntity.ok(services);
    }
}