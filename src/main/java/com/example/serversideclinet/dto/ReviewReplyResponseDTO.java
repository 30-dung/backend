// src/main/java/com/example/serversideclinet/dto/ReviewReplyResponseDTO.java
package com.example.serversideclinet.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewReplyResponseDTO {
    private Integer replyId;
    private Integer reviewId;
    private UserInfoDTO replier;
    private String comment;
    private LocalDateTime createdAt;
    private Boolean isStoreReply;
    private Integer parentReplyId; // THÊM TRƯỜNG NÀY
    private List<ReviewReplyResponseDTO> childrenReplies; // THÊM TRƯỜNG NÀY
}