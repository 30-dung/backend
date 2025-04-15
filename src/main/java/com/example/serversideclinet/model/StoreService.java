package com.example.serversideclinet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// StoreService.java
@Entity
@Table(name = "StoreService",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "service_id"}))
public class StoreService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer storeServiceId;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(nullable = false)
    private BigDecimal price;

    // Getters and Setters
}
