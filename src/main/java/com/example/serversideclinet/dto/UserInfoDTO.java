// src/main/java/com/example/serversideclinet/dto/UserInfoDTO.java
package com.example.serversideclinet.dto;

import lombok.Data;

@Data
public class UserInfoDTO {
    private Integer userId; // Đảm bảo trường này tồn tại
    private String fullName;
    private String email;
    // private String avatarUrl; // Thêm nếu User entity có
}