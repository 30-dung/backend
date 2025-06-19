// src/main/java/com/example/serversideclinet/model/ReviewReply.java
package com.example.serversideclinet.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "review_replies")
public class ReviewReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer replyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Review review;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String comment;

    private Boolean isStoreReply = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Để trỏ đến cha (nếu đây là câu trả lời cho một reply khác)
    @ManyToOne(fetch = FetchType.LAZY) // Lazy để tránh vòng lặp vô hạn khi tải toàn bộ cây
    @JoinColumn(name = "parent_reply_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ReviewReply parentReply;

    // Chứa các câu trả lời con. EAGER để tải cùng với cha trong một truy vấn.
    // LƯU Ý: Với nhiều cấp độ và số lượng replies lớn, EAGER có thể gây hiệu năng.
    // Nếu gặp hiệu năng, cần dùng @EntityGraph trên Repository hoặc tải thủ công.
    @OneToMany(mappedBy = "parentReply", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ReviewReply> childrenReplies;
}