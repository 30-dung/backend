package com.example.serversideclinet.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_bonus_config")
public class PerformanceBonusConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "points_threshold", nullable = false)
    private Integer pointsThreshold;

    @Column(name = "bonus_amount", nullable = false)
    private BigDecimal bonusAmount;

    @Column(name = "is_percentage", nullable = false)
    private Boolean isPercentage = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public PerformanceBonusConfig() {
    }

    public PerformanceBonusConfig(Integer pointsThreshold, BigDecimal bonusAmount, Boolean isPercentage) {
        this.pointsThreshold = pointsThreshold;
        this.bonusAmount = bonusAmount;
        this.isPercentage = isPercentage;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPointsThreshold() {
        return pointsThreshold;
    }

    public void setPointsThreshold(Integer pointsThreshold) {
        this.pointsThreshold = pointsThreshold;
    }

    public BigDecimal getBonusAmount() {
        return bonusAmount;
    }

    public void setBonusAmount(BigDecimal bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public Boolean getIsPercentage() {
        return isPercentage;
    }

    public void setIsPercentage(Boolean percentage) {
        isPercentage = percentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method to calculate bonus based on points
    public BigDecimal calculateBonus(BigDecimal baseSalary, Integer points) {
        if (points < pointsThreshold) {
            return BigDecimal.ZERO;
        }

        if (isPercentage) {
            // Calculate percentage of base salary
            return baseSalary.multiply(bonusAmount.divide(BigDecimal.valueOf(100)));
        } else {
            // Fixed amount
            return bonusAmount;
        }
    }
}