// src/main/java/com/example/serversideclinet/model/Review.java
package com.example.serversideclinet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"appointment_id", "target_id", "target_type"})
})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReviewTargetType targetType;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Replies của Review (gốc)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ReviewReply> replies;

    public Review() {}

    public Review(User user, Appointment appointment, Integer targetId, ReviewTargetType targetType, Integer rating, String comment) {
        this.user = user;
        this.appointment = appointment;
        this.targetId = targetId;
        this.targetType = targetType;
        this.rating = rating;
        this.comment = comment;
    }
}