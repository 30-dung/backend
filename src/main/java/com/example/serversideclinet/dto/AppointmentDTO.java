//// Tạo DTO cho Appointment
//package com.example.serversideclinet.dto;
//
//import com.example.serversideclinet.model.Appointment;
//import java.time.LocalDateTime;
//
//
//public class AppointmentDTO {
//    private Integer appointmentId;
//    private Integer userId;
//    private String userName;
//    private String userEmail;
//    private Integer employeeId;
//    private String employeeName;
//    private Integer storeServiceId;
//    private String serviceName;
//    private LocalDateTime startTime;
//    private LocalDateTime endTime;
//    private String status;
//    private String notes;
//
//    // Constructors
//    public AppointmentDTO() {}
//
//    public AppointmentDTO(Appointment appointment) {
//        this.appointmentId = appointment.getAppointmentId();
//        if (appointment.getUser() != null) {
//            this.userId = appointment.getUser().getUserId();
//            this.userName = appointment.getUser().getFullName();
//            this.userEmail = appointment.getUser().getEmail();
//        }
//
//        if (appointment.getEmployee() != null) {
//            this.employeeId = appointment.getEmployee().getEmployeeId();
//            this.employeeName = appointment.getEmployee().getFullName();
//        }
//
//        if (appointment.getStoreService() != null) {
//            this.storeServiceId = appointment.getStoreService().getStoreServiceId();
//            if (appointment.getStoreService().getService() != null) {
//                this.serviceName = appointment.getStoreService().getService().getServiceName();
//            }
//        }
//
//        if (appointment.getAppointmentTimeSlot() != null) {
//            this.startTime = appointment.getAppointmentTimeSlot().getStartTime();
//            this.endTime = appointment.getAppointmentTimeSlot().getEndTime();
//        }
//
//        this.status = appointment.getStatus().name();
//        this.notes = appointment.getNotes();
//    }
//    // Getters và Setters
//    public Integer getAppointmentId() {
//        return appointmentId;
//    }
//
//    public void setAppointmentId(Integer appointmentId) {
//        this.appointmentId = appointmentId;
//    }
//
//    public Integer getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Integer userId) {
//        this.userId = userId;
//    }
//
//    public String getUserName() {
//        return userName;
//    }
//
//    public void setUserName(String userName) {
//        this.userName = userName;
//    }
//
//    public String getUserEmail() {
//        return userEmail;
//    }
//
//    public void setUserEmail(String userEmail) {
//        this.userEmail = userEmail;
//    }
//
//    public Integer getEmployeeId() {
//        return employeeId;
//    }
//
//    public void setEmployeeId(Integer employeeId) {
//        this.employeeId = employeeId;
//    }
//
//    public String getEmployeeName() {
//        return employeeName;
//    }
//
//    public void setEmployeeName(String employeeName) {
//        this.employeeName = employeeName;
//    }
//
//    public Integer getStoreServiceId() {
//        return storeServiceId;
//    }
//
//    public void setStoreServiceId(Integer storeServiceId) {
//        this.storeServiceId = storeServiceId;
//    }
//
//    public String getServiceName() {
//        return serviceName;
//    }
//
//    public void setServiceName(String serviceName) {
//        this.serviceName = serviceName;
//    }
//
//    public LocalDateTime getStartTime() {
//        return startTime;
//    }
//
//    public void setStartTime(LocalDateTime startTime) {
//        this.startTime = startTime;
//    }
//
//    public LocalDateTime getEndTime() {
//        return endTime;
//    }
//
//    public void setEndTime(LocalDateTime endTime) {
//        this.endTime = endTime;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getNotes() {
//        return notes;
//    }
//
//    public void setNotes(String notes) {
//        this.notes = notes;
//    }
//}
