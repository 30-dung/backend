// src/main/java/com/example/serversideclinet/dto/ReviewResponseDTO.java
package com.example.serversideclinet.dto;

import com.example.serversideclinet.model.ReviewTargetType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewResponseDTO {
    private Integer reviewId;
    private UserInfoDTO reviewer; // Thông tin người đánh giá
    private Integer appointmentId; // ID lịch hẹn liên quan
    private Integer targetId;
    private ReviewTargetType targetType;
    private String targetName; // e.g., Employee's full name, Service's name, Store's name
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private List<ReviewReplyResponseDTO> replies; // Danh sách các phản hồi cho đánh giá này

    // Thông tin bổ sung để hiển thị trên frontend
    private String storeName;
    private Integer storeId;
    private String employeeName; // Tên nhân viên (nếu đánh giá liên quan đến employee)
    private String serviceName; // Tên dịch vụ (nếu đánh giá liên quan đến service)
}