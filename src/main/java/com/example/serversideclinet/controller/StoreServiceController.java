package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.StoreServiceRequest;
import com.example.serversideclinet.model.StoreService;
import com.example.serversideclinet.service.StoreServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequestMapping("api/store-service")
public class StoreServiceController {

    @Autowired
    private StoreServiceService storeServiceService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/price")
    public ResponseEntity<StoreService> createPrice(
            @RequestBody StoreServiceRequest request
    ) {
        StoreService created = storeServiceService.createStoreService(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/all") // <<< Đảm bảo endpoint này tồn tại
    public ResponseEntity<List<StoreService>> getAllStoreServices() {
        List<StoreService> storeServices = storeServiceService.getAllStoreServices();
        return ResponseEntity.ok(storeServices);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<StoreService>> getServicesByStore(@PathVariable Integer storeId) {
        List<StoreService> services = storeServiceService.getServicesByStoreId(storeId);
        return ResponseEntity.ok(services);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStoreService(@PathVariable Integer id) {
        if (storeServiceService.deleteStoreService(id)) {
            return ResponseEntity.ok("StoreService deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStoreService(@PathVariable Integer id, @RequestBody StoreServiceRequest request) {
        StoreService updated = storeServiceService.updateStoreService(id, request);
        if (updated != null) {
            return ResponseEntity.ok("StoreService updated successfully");
        }
        return ResponseEntity.notFound().build();
    }
}