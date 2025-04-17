package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.ServiceRequest;
import com.example.serversideclinet.model.ServiceEntity;
import com.example.serversideclinet.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/services")
@PreAuthorize("hasRole('ADMIN')")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @PostMapping
    public ResponseEntity<?> createService(@RequestBody ServiceRequest request) {
        ServiceEntity service = serviceService.create(request);
        return ResponseEntity.ok(service);
    }

    @GetMapping
    public ResponseEntity<?> getAllServices() {
        return ResponseEntity.ok(serviceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceById(@PathVariable Integer id) {
        return serviceService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(@PathVariable Integer id, @RequestBody ServiceRequest request) {
        return serviceService.update(id, request)
                .map(updated -> ResponseEntity.ok("Updated successfully"))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Integer id) {
        if (serviceService.delete(id)) {
            return ResponseEntity.ok("Deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}

