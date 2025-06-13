package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.Invoice;
import com.example.serversideclinet.model.InvoiceStatus;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Invoice>> getAllInvoicesForUser() {
        User user = getUserFromPrincipal();
        List<Invoice> invoices = invoiceService.findAllByUser(user);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Invoice>> getPendingInvoices() {
        User user = getUserFromPrincipal();
        List<Invoice> invoices = invoiceService.findByStatus(user, InvoiceStatus.PENDING);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/paid")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Invoice>> getPaidInvoices() {
        User user = getUserFromPrincipal();
        List<Invoice> invoices = invoiceService.findByStatus(user, InvoiceStatus.PAID);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/cancelled")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Invoice>> getCancelledInvoices() {
        User user = getUserFromPrincipal();
        List<Invoice> invoices = invoiceService.findByStatus(user, InvoiceStatus.CANCELED);
        return ResponseEntity.ok(invoices);
    }

    private User getUserFromPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = new User();
        user.setUserId(userDetails.getUserId());
        user.setEmail(userDetails.getUsername());
        return user;
    }
}
