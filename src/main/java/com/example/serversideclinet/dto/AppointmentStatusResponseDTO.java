package com.example.serversideclinet.dto;

public class AppointmentStatusResponseDTO {
    private Integer appointmentId;
    private String status;  // PENDING, CONFIRMED, CANCELED, COMPLETED
    private String startTime;
    private String endTime;
    private String completedAt;  // New field for completion timestamp
    private String employeeName;
    private String userName;
    private String serviceName;
    private String notes;

    // Constructor
    public AppointmentStatusResponseDTO(Integer appointmentId, String status,
                                        String startTime, String endTime, String completedAt,
                                        String employeeName, String userName,
                                        String serviceName) {
        this.appointmentId = appointmentId;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.completedAt = completedAt;
        this.employeeName = employeeName;
        this.userName = userName;
        this.serviceName = serviceName;
        this.notes = notes;
    }

    // Getters and Setters
    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}