package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.OrderDTO;
import com.example.serversideclinet.security.CustomPrincipal;
import com.example.serversideclinet.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(Authentication authentication) {
        Integer userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(orderService.createOrder(userId));
    }

    private Integer getUserIdFromAuthentication(Authentication authentication) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        return principal.getUserId();
    }
}
