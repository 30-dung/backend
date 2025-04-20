package com.example.serversideclinet.dto;

public class AppointmentRequest {
    private Integer timeSlotId;
    private Integer storeServiceId;
    private String notes;
    // getters + setters

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
}
