package com.example.serversideclinet.model;
import jakarta.persistence.*;

import java.math.BigDecimal;

// InvoiceDetail.java
@Entity
@Table(name = "InvoiceDetail")
public class InvoiceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer detailId;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @OneToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "store_service_id")
    private StoreService storeService;

    private String description;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    private Integer quantity = 1;

    // Getters and Setters
}
