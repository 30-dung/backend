package com.example.serversideclinet.model;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_records")
public class SalaryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer salaryRecordId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private Employee employee;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private Appointment appointment;

    @Column(nullable = false)
    private BigDecimal serviceAmount; // Giá trị dịch vụ

    @Column(nullable = false)
    private BigDecimal commissionAmount; // Số tiền hoa hồng thực tế

    @Column(nullable = false)
    private BigDecimal commissionRate; // Tỷ lệ hoa hồng áp dụng

    @Column(nullable = false)
    private LocalDate workDate; // Ngày làm việc

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime paidAt; // Ngày trả lương

    private String notes; // Ghi chú

    public enum PaymentStatus {
        PENDING,    // Chờ thanh toán
        PAID,       // Đã thanh toán
        CANCELLED   // Hủy bỏ
    }

    // Getters and Setters
    public Integer getSalaryRecordId() {
        return salaryRecordId;
    }

    public void setSalaryRecordId(Integer salaryRecordId) {
        this.salaryRecordId = salaryRecordId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public BigDecimal getServiceAmount() {
        return serviceAmount;
    }

    public void setServiceAmount(BigDecimal serviceAmount) {
        this.serviceAmount = serviceAmount;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}