// src/main/java/com/example/serversideclinet/dto/ServiceRatingSummaryDTO.java
package com.example.serversideclinet.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceRatingSummaryDTO {
    private Integer serviceId; // ID của ServiceEntity
    private String serviceName;
    private BigDecimal averageRating;
    private Long totalReviews;
    private String serviceImg;
    private String description;
    private Short durationMinutes;
}