// src/main/java/com/example/serversideclinet/dto/CombinedReviewDisplayDTO.java
package com.example.serversideclinet.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CombinedReviewDisplayDTO {
    private Integer mainReviewId; // ID của review chính (ví dụ: review STORE), dùng để reply
    private UserInfoDTO reviewer;
    private Integer appointmentId;
    private String appointmentSlug;

    private String storeName;
    private Integer storeId;
    private String employeeName;
    private Integer employeeId;
    private String serviceName;
    private Integer storeServiceId;

    private Integer storeRating;
    private Integer employeeRating;
    private Integer serviceRating;

    private String comment;
    private LocalDateTime createdAt;

    private List<ReviewReplyResponseDTO> replies; // Danh sách replies gốc (đã được xây dựng cây)

    public CombinedReviewDisplayDTO() {}

    public CombinedReviewDisplayDTO(UserInfoDTO reviewer, Integer appointmentId, String appointmentSlug,
                                    String storeName, Integer storeId, String employeeName, Integer employeeId,
                                    String serviceName, Integer storeServiceId,
                                    String comment, LocalDateTime createdAt, List<ReviewReplyResponseDTO> replies,
                                    Integer mainReviewId) {
        this.reviewer = reviewer;
        this.appointmentId = appointmentId;
        this.appointmentSlug = appointmentSlug;
        this.storeName = storeName;
        this.storeId = storeId;
        this.employeeName = employeeName;
        this.employeeId = employeeId;
        this.serviceName = serviceName;
        this.storeServiceId = storeServiceId;
        this.comment = comment;
        this.createdAt = createdAt;
        this.replies = replies;
        this.mainReviewId = mainReviewId;
    }
}