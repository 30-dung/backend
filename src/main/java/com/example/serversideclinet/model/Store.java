package com.example.serversideclinet.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "Store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer storeId;
    @Column(nullable = false)
    private String storeName;
    @Column(nullable = false)
    private String storeImages;
    @Column(nullable = false)
    private String phoneNumber;

    private String cityProvince;
    private String district;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String description;

    @Column(columnDefinition = "DECIMAL(3,2) default 0.00")
    private BigDecimal averageRating = BigDecimal.ZERO;

    // THÊM TRƯỜNG TOTAL_REVIEWS CHO STORE VÀO ĐÂY
    @Column(columnDefinition = "BIGINT default 0")
    private Long totalReviews = 0L;
    // KẾT THÚC THÊM TRƯỜNG

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCityProvince() {
        return cityProvince;
    }

    public void setCityProvince(String cityProvince) {
        this.cityProvince = cityProvince;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    // THÊM GETTER VÀ SETTER CHO totalReviews CỦA STORE VÀO ĐÂY
    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }
    // KẾT THÚC THÊM GETTER VÀ SETTER

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStoreImages() {
        return storeImages;
    }

    public void setStoreImages(String storeImages) {
        this.storeImages = storeImages;
    }
}