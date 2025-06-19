// src/main/java/com/example/serversideclinet/model/ReviewTargetType.java
package com.example.serversideclinet.model;

public enum ReviewTargetType {
    STORE,        // Đánh giá tổng thể cửa hàng
    EMPLOYEE,     // Đánh giá nhân viên
    SERVICE,      // Giữ lại nếu bạn có nhu cầu đánh giá ServiceEntity độc lập với StoreService
    STORE_SERVICE // Đánh giá dịch vụ cụ thể của một cửa hàng (Cần thiết cho hệ thống này)
}