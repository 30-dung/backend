package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.StoreServiceRequest;
import com.example.serversideclinet.model.StoreService;
import com.example.serversideclinet.service.StoreServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("api/store-service")
public class StoreServiceController {

    @Autowired
    private StoreServiceService storeServiceService;

    // Tạo mới StoreService (ADMIN)
    @PostMapping("/price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreService> createPrice(@RequestBody StoreServiceRequest request) {
        StoreService created = storeServiceService.createStoreService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @GetMapping
// Xoá hoặc comment dòng PreAuthorize
// @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<StoreService>> getStoreServiceStore() {
        List<StoreService> services = storeServiceService.getAllStoreServiceStore();
        return ResponseEntity.ok(services);
    }

}
