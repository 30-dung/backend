package com.example.serversideclinet.dto;

import java.time.LocalDateTime;

public class AppointmentRequest {
    private Integer timeSlotId;
    private Integer storeServiceId;
    private String notes;
    private LocalDateTime startTime;  // Thêm startTime
    private LocalDateTime endTime;    // Thêm endTime

    // Getters and Setters
    public Integer getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Integer timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public Integer getStoreServiceId() {
        return storeServiceId;
    }

    public void setStoreServiceId(Integer storeServiceId) {
        this.storeServiceId = storeServiceId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Chỉnh sửa kiểu trả về của getStartTime() từ CharSequence thành LocalDateTime
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
}
