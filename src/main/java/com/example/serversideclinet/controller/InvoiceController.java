package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.Invoice;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/create")
    public ResponseEntity<Invoice> createInvoice(Principal principal) {
        User user = getUserFromPrincipal();
        Invoice invoice = invoiceService.createInvoiceFromPendingAppointments(user);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/pay/{invoiceId}")
    public ResponseEntity<Invoice> payInvoice(@PathVariable Integer invoiceId) {
        Invoice paid = invoiceService.markInvoiceAsPaid(invoiceId);
        return ResponseEntity.ok(paid);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Invoice>> getPendingInvoices(Principal principal) {
        User user = getUserFromPrincipal();
        List<Invoice> invoices = invoiceService.findUnpaidInvoicesByUser(user);
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
