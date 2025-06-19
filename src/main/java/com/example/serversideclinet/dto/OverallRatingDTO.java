// src/main/java/com/example/serversideclinet/dto/OverallRatingDTO.java
package com.example.serversideclinet.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OverallRatingDTO {
    private Integer storeId;
    private String storeName;
    private String storeImageUrl; // THÊM TRƯỜNG NÀY
    private BigDecimal averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingDistribution;
    private List<EmployeeRatingSummaryDTO> employeeRatings;
    private List<ServiceRatingSummaryDTO> serviceRatings;
}