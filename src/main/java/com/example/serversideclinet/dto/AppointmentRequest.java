package com.example.serversideclinet.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AppointmentRequest {
    // Getters and Setters
    private Integer timeSlotId;
    private Integer storeServiceId;
    private String notes;
    private LocalDateTime startTime;  // Thời gian bắt đầu cụ thể cho cuộc hẹn

    public void setTimeSlotId(Integer timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public void setStoreServiceId(Integer storeServiceId) {
        this.storeServiceId = storeServiceId;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Integer getTimeSlotId() {
        return timeSlotId;
    }

    public Integer getStoreServiceId() {
        return storeServiceId;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}