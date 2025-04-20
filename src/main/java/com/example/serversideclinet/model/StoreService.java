package com.example.serversideclinet.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

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
    private ServiceEntity service;

    @Column(nullable = false)
    private BigDecimal price;

    // Getter and Setter methods

    public Integer getStoreServiceId() {
        return storeServiceId;
    }

    public void setStoreServiceId(Integer storeServiceId) {
        this.storeServiceId = storeServiceId;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // Override equals and hashCode based on storeServiceId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreService that = (StoreService) o;
        return Objects.equals(storeServiceId, that.storeServiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeServiceId);
    }

    // toString method
    @Override
    public String toString() {
        return "StoreService{" +
                "storeServiceId=" + storeServiceId +
                ", store=" + store +
                ", service=" + service +
                ", price=" + price +
                '}';
    }


}
