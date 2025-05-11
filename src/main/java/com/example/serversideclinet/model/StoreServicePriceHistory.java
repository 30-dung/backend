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

    // Getters and Setters\

    public Integer getPriceHistoryId() {
        return priceHistoryId;
    }

    public void setPriceHistoryId(Integer priceHistoryId) {
        this.priceHistoryId = priceHistoryId;
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
