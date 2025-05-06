package com.example.serversideclinet.controller;// package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.StoreServiceRequest;
import com.example.serversideclinet.model.StoreService;
import com.example.serversideclinet.service.StoreServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/admin/create/price/store")
@PreAuthorize("hasRole('ADMIN')")
public class StoreServiceController {

    @Autowired
    private StoreServiceService storeServiceService;

    @PostMapping
    public ResponseEntity<StoreService> createPrice(
            @RequestBody StoreServiceRequest request
    ) {
        StoreService created = storeServiceService.createStoreService(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

}
