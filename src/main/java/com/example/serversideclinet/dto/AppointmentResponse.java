package com.example.serversideclinet.dto;

import lombok.Data;

@Data
public class AppointmentResponse {
    private Integer appointmentId;
    private String slug;
    private String startTime;
    private String endTime;
    private String status;
    private String createdAt;
    private StoreServiceDetail storeService;
    private EmployeeDetail employee;
    private InvoiceDetailInfo invoice;
    private UserDetail user;

    @Data
    public static class StoreServiceDetail {
        private Integer storeId;
        private Integer storeServiceId;
        private String storeName;
        private String serviceName;

        public StoreServiceDetail(Integer storeId, Integer storeServiceId, String storeName, String serviceName) {
            this.storeId = storeId;
            this.storeServiceId = storeServiceId;
            this.storeName = storeName;
            this.serviceName = serviceName;
        }
    }

    @Data
    public static class EmployeeDetail {
        private Integer employeeId;
        private String fullName;
        private String email; // THÊM TRƯỜNG EMAIL VÀO ĐÂY

        public EmployeeDetail(Integer employeeId, String fullName) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.email = null; // Khởi tạo mặc định
        }

        public EmployeeDetail(Integer employeeId, String fullName, String email) { // Constructor mới
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.email = email;
        }
    }

    @Data
    public static class InvoiceDetailInfo {
        private double totalAmount;

        public InvoiceDetailInfo(double totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    @Data
    public static class UserDetail {
        private Integer userId;
        private String fullName;

        public UserDetail(Integer userId, String fullName) {
            this.userId = userId;
            this.fullName = fullName;
        }
    }
}