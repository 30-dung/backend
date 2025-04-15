package com.example.serversideclinet.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Review.java
@Entity
@Table(name = "Review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReviewTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @Column(nullable = false)
    @Min(1) @Max(5)
    private Integer rating;

    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Getters and Setters
}
