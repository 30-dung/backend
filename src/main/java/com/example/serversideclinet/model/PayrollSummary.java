package com.example.serversideclinet.model;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_summary")
public class PayrollSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer payrollId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate periodStartDate; // Ngày bắt đầu kỳ lương

    @Column(nullable = false)
    private LocalDate periodEndDate; // Ngày kết thúc kỳ lương

    @Column(nullable = false)
    private BigDecimal baseSalary = BigDecimal.ZERO; // Lương cơ bản

    @Column(nullable = false)
    private BigDecimal totalCommission = BigDecimal.ZERO; // Tổng hoa hồng

    @Column(nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO; // Tổng lương

    @Column(nullable = false)
    private Integer totalAppointments = 0; // Tổng số cuộc hẹn hoàn thành

    @Column(nullable = false)
    private BigDecimal totalRevenue = BigDecimal.ZERO; // Tổng doanh thu từ nhân viên

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime approvedAt; // Ngày duyệt

    private LocalDateTime paidAt; // Ngày trả lương

    @ManyToOne
    @JoinColumn(name = "approved_by")
    @JsonIdentityReference(alwaysAsId = true)
    private Employee approvedBy; // Người duyệt

    private String notes; // Ghi chú

    public enum PayrollStatus {
        DRAFT,      // Bản nháp
        PENDING,    // Chờ duyệt
        APPROVED,   // Đã duyệt
        PAID,       // Đã trả
        CANCELLED   // Hủy bỏ
    }

    // Getters and Setters
    public Integer getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(Integer payrollId) {
        this.payrollId = payrollId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public LocalDate getPeriodEndDate() {
        return periodEndDate;
    }

    public void setPeriodEndDate(LocalDate periodEndDate) {
        this.periodEndDate = periodEndDate;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getTotalCommission() {
        return totalCommission;
    }

    public void setTotalCommission(BigDecimal totalCommission) {
        this.totalCommission = totalCommission;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(Integer totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public PayrollStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public Employee getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Employee approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}