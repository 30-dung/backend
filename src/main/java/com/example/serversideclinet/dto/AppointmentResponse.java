package com.example.serversideclinet.dto;

public class AppointmentResponse {
    private Integer appointmentId;
    private String slug;
    private String startTime;
    private String endTime;
    private String status;
    private String createdAt; // Thêm trường createdAt
    private StoreService storeService;
    private Employee employee;
    private Invoice invoice;

    // Nested classes
    public static class StoreService {
        private String storeName;
        private String serviceName;

        public StoreService(String storeName, String serviceName) {
            this.storeName = storeName;
            this.serviceName = serviceName;
        }

        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    }

    public static class Employee {
        private String fullName;

        public Employee(String fullName) {
            this.fullName = fullName;
        }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }

    public static class Invoice {
        private double totalAmount;

        public Invoice(double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    }

    // Getters and Setters
    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; } // Getter cho createdAt
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; } // Setter cho createdAt
    public StoreService getStoreService() { return storeService; }
    public void setStoreService(StoreService storeService) { this.storeService = storeService; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
}