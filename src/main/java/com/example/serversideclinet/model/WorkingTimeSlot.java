package com.example.serversideclinet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "WorkingTimeSlot")
public class WorkingTimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer timeSlotId;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    // Thêm quan hệ OneToMany với AppointmentTimeSlot
    @OneToMany(mappedBy = "workingTimeSlot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentTimeSlot> appointmentTimeSlots = new ArrayList<>();

    private Boolean isActive = true;

    // Thêm phương thức tiện ích để kiểm tra sự khả dụng
    public Boolean checkAvailability(LocalDateTime start, LocalDateTime end) {
        if (!isActive) return false;

        for (AppointmentTimeSlot slot : appointmentTimeSlots) {
            if (slot.getIsBooked() &&
                    (start.isBefore(slot.getEndTime()) && end.isAfter(slot.getStartTime()))) {
                return false; // Có xung đột thời gian
            }
        }
        return true; // Không có xung đột
    }

    // Thêm phương thức tạo slot hẹn mới
    public AppointmentTimeSlot createAppointmentSlot(LocalDateTime start, LocalDateTime end) {
        if (!checkAvailability(start, end)) {
            return null; // Không thể tạo slot mới vì đã có xung đột
        }

        AppointmentTimeSlot newSlot = new AppointmentTimeSlot();
        newSlot.setWorkingTimeSlot(this);
        newSlot.setStartTime(start);
        newSlot.setEndTime(end);
        newSlot.setIsBooked(true);
        appointmentTimeSlots.add(newSlot);
        return newSlot;
    }

    // Getters and Setters
    public Integer getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Integer timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public List<AppointmentTimeSlot> getAppointmentTimeSlots() {
        return appointmentTimeSlots;
    }

    public void setAppointmentTimeSlots(List<AppointmentTimeSlot> appointmentTimeSlots) {
        this.appointmentTimeSlots = appointmentTimeSlots;
    }
}