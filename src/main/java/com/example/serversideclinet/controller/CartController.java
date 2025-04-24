package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.CartDTO;
import com.example.serversideclinet.security.CustomPrincipal;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public ResponseEntity<CartDTO> getCart(Authentication authentication) {
        Integer userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<CartDTO> addToCart(
            Authentication authentication,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        Integer userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(cartService.addToCart(userId, productId, quantity));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<CartDTO> removeFromCart(
            Authentication authentication,
            @PathVariable Long itemId) {
        Integer userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(cartService.removeFromCart(userId, itemId));
    }

    private Integer getUserIdFromAuthentication(Authentication authentication) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        return principal.getUserId();
    }
}
