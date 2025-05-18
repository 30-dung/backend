package com.example.serversideclinet.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer appointmentId;

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
    // Add JsonBackReference to break the circular reference with WorkingTimeSlot
    @JsonBackReference("slot-appointments")
    private WorkingTimeSlot workingSlot;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "store_service_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private StoreService storeService;

    private String notes;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private boolean reminderSent = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Appointment(Integer appointmentId) {
    }

    public Appointment() {

    }

    public enum Status {
        PENDING, CONFIRMED, COMPLETED, CANCELED
    }

    // Getters and Setters remain the same
    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
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
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}