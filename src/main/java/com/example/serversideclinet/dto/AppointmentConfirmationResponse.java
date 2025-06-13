package com.example.serversideclinet.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AppointmentConfirmationResponse {
    private Integer invoiceId;
    private String userEmail;
    private Double totalAmount;
    private String status;
    private List<AppointmentDetail> appointments;

    public static class AppointmentDetail {
        private Integer appointmentId;
        private String storeName;
        private String serviceName;
        private String employeeName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double price;

        public Integer getAppointmentId() {
            return appointmentId;
        }

        public void setAppointmentId(Integer appointmentId) {
            this.appointmentId = appointmentId;
        }

        public String getStoreName() {
            return storeName;
        }

        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
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

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AppointmentDetail> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<AppointmentDetail> appointments) {
        this.appointments = appointments;
    }
}