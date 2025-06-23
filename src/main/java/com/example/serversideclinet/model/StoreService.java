package com.example.serversideclinet.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "StoreService",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "service_id"}))
public class StoreService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer storeServiceId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;
    @Column(nullable = false)
    private BigDecimal price;

    // THÊM TRƯỜNG AVERAGE_RATING VÀ TOTAL_REVIEWS CHO STORE_SERVICE VÀO ĐÂY
    @Column(columnDefinition = "DECIMAL(3,2) default 0.00")
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(columnDefinition = "BIGINT default 0")
    private Long totalReviews = 0L;
    // KẾT THÚC THÊM TRƯỜNG

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

    // SỬA DÒNG NÀY (sửa lỗi chính tả từ voidsetService thành setService)
    public void setService(ServiceEntity service) {
        this.service = service;
    }
    // KẾT THÚC SỬA DÒNG NÀY

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // THÊM GETTERS VÀ SETTERS CHO CÁC TRƯỜNG MỚI VÀO ĐÂY
    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }
    // KẾT THÚC THÊM GETTERS VÀ SETTERS

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

    // toString method - CẬP NHẬT ĐỂ BAO GỒM CÁC TRƯỜNG MỚI
    @Override
    public String toString() {
        return "StoreService{" +
                "storeServiceId=" + storeServiceId +
                ", store=" + store +
                ", service=" + service +
                ", price=" + price +
                ", averageRating=" + averageRating + // THÊM DÒNG NÀY
                ", totalReviews=" + totalReviews +   // THÊM DÒNG NÀY
                '}';
    }
}