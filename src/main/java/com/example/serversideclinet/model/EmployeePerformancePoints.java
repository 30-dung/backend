package com.example.serversideclinet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "employee_performance_points",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "yearMonthStr"}))
public class EmployeePerformancePoints {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Column(name = "yearMonthStr", nullable = false)
    private String yearMonth; // Format: YYYY-MM

    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public EmployeePerformancePoints() {
    }

    public EmployeePerformancePoints(Employee employee, String yearMonth) {
        this.employee = employee;
        this.yearMonth = yearMonth;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public Boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Boolean processed) {
        isProcessed = processed;
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

    // Helper methods
    public void addPoints(Integer points) {
        this.totalPoints += points;
    }

    // Static helper method to get the current year-month string
    public static String getCurrentYearMonthString() {
        YearMonth currentYearMonth = YearMonth.now();
        return currentYearMonth.toString(); // Returns in YYYY-MM format
    }
}