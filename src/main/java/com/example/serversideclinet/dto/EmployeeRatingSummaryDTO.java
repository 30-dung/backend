package com.example.serversideclinet.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmployeeRatingSummaryDTO {
    private Integer employeeId;
    private String employeeName;
    private BigDecimal averageRating;
    private Long totalReviews;
    private String avatarUrl;

}