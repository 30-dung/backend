package com.example.serversideclinet.model;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Invoice.java
@Entity
@Table(name = "Invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer invoiceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "invoice")
    private List<InvoiceDetail> invoiceDetails = new ArrayList<>();

    // Getters and Setters
}