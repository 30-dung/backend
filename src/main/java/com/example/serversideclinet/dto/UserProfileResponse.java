// src/main/java/com/example/serversideclinet/dto/UserProfileResponse.java
package com.example.serversideclinet.dto;

import lombok.Data;

@Data
public class UserProfileResponse {
    private Integer userId; // THÊM TRƯỜNG NÀY
    private String fullName;
    private String email;
    private String phoneNumber;
    private String membershipType;
    private Integer loyaltyPoints;

    public UserProfileResponse(Integer userId, String fullName, String email, String phoneNumber, String membershipType, Integer loyaltyPoints) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.membershipType = membershipType;
        this.loyaltyPoints = loyaltyPoints;
    }
}