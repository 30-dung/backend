package com.example.serversideclinet.dto;

import com.example.serversideclinet.model.ReviewTargetType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequestDTO {
    @NotNull(message = "User ID cannot be null")
    private Integer userId; // ID của người dùng đánh giá
    @NotNull(message = "Appointment ID cannot be null")
    private Integer appointmentId; // ID của lịch hẹn mà đánh giá này liên quan
    @NotNull(message = "Target ID cannot be null")
    private Integer targetId; // ID của đối tượng được đánh giá (EmployeeId, StoreServiceId, hoặc StoreId)
    @NotNull(message = "Target Type cannot be null")
    private ReviewTargetType targetType; // Loại đối tượng được đánh giá
    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private Integer rating; // Số sao từ 1 đến 5
    private String comment; // Bình luận
}