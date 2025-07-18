package com.example.serversideclinet.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer appointmentId;

    @Column(unique = true, nullable = false, length = 10)
    private String slug;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "working_slot_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    @JsonBackReference("slot-appointments")
    private WorkingTimeSlot workingSlot;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonBackReference("invoice-appointments")
    private Invoice invoice;

    @Column(nullable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime startTime;

    @Column(nullable = false)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_service_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private StoreService storeService;

    private String notes;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    // Thêm field để track xem lương đã được tính hay chưa
    @Column(name = "salary_calculated", nullable = false)
    private boolean salaryCalculated = false;

    // Thêm field để lưu thời gian hoàn thành
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime completedAt;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Appointment(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Appointment() {
    }

    // Fix method canCalculateSalary
    public boolean canCalculateSalary() {
        return this.status == Status.COMPLETED && !this.salaryCalculated;
    }

    public void setSalaryCalculated(boolean salaryCalculated) {
        this.salaryCalculated = salaryCalculated;
    }

    public boolean isSalaryCalculated() {
        return salaryCalculated;
    }

    // Fix getCompletedAt method
    public OffsetDateTime getCompletedAt() {
        if (completedAt != null) {
            return completedAt.atOffset(java.time.ZoneOffset.UTC);
        }
        return null;
    }

    public LocalDateTime getCompletedAtLocal() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    // THÊM TRẠNG THÁI REJECTED
    public enum Status {
        PENDING, CONFIRMED, COMPLETED, CANCELED, REJECTED // Thêm REJECTED
    }

    // Getters and Setters
    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public WorkingTimeSlot getWorkingSlot() {
        return workingSlot;
    }

    public void setWorkingSlot(WorkingTimeSlot workingSlot) {
        this.workingSlot = workingSlot;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        // Tự động set completedAt khi status thành COMPLETED
        if (status == Status.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}