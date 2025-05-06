package com.example.serversideclinet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AppointmentTimeSlot")
public class AppointmentTimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer slotId;

    @ManyToOne
    @JoinColumn(name = "working_time_slot_id", nullable = false)
    private WorkingTimeSlot workingTimeSlot;

    @OneToOne(mappedBy = "appointmentTimeSlot")
    private Appointment appointment;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private Boolean isBooked = false;

    // Getters and Setters
    public Integer getSlotId() {
        return slotId;
    }

    public void setSlotId(Integer slotId) {
        this.slotId = slotId;
    }

    public WorkingTimeSlot getWorkingTimeSlot() {
        return workingTimeSlot;
    }

    public void setWorkingTimeSlot(WorkingTimeSlot workingTimeSlot) {
        this.workingTimeSlot = workingTimeSlot;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
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

    public Boolean getIsBooked() {
        return isBooked;
    }

    public void setIsBooked(Boolean booked) {
        isBooked = booked;
    }
}