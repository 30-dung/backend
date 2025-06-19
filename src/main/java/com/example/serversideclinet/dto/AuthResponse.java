// src/main/java/com/example/serversideclinet/dto/AuthResponse.java
package com.example.serversideclinet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private Integer userId; // Đã có
    private String fullName; // Đã có
    private String message;

    public AuthResponse(String token, String role, Integer userId, String fullName) {
        this.token = token;
        this.role = role;
        this.userId = userId;
        this.fullName = fullName;
        this.message = null;
    }

    public AuthResponse(String token, String role, String message) {
        this.token = token;
        this.role = role;
        this.userId = null;
        this.fullName = null;
        this.message = message;
    }

    public AuthResponse(String message) {
        this.token = null;
        this.role = null;
        this.userId = null;
        this.fullName = null;
        this.message = message;
    }
}