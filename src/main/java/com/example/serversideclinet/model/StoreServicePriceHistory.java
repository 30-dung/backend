package com.example.serversideclinet.model;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


// StoreServicePriceHistory.java
@Entity
@Table(name = "StoreServicePriceHistory")
public class StoreServicePriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer priceHistoryId;

    @ManyToOne
    @JoinColumn(name = "store_service_id", nullable = false)
    private StoreService storeService;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime effectiveDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Getters and Setters
}
