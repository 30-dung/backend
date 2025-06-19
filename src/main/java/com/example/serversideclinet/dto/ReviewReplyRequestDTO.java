// src/main/java/com/example/serversideclinet/dto/ReviewReplyRequestDTO.java
package com.example.serversideclinet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewReplyRequestDTO {
    @NotNull(message = "Review ID cannot be null")
    private Integer reviewId;
    @NotNull(message = "User ID cannot be null")
    private Integer userId;
    @NotBlank(message = "Comment cannot be empty")
    private String comment;
    private Boolean isStoreReply = false;
    private Integer parentReplyId; // THÊM TRƯỜNG NÀY
}