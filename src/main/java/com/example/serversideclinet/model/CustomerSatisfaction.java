package com.example.serversideclinet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "customer_satisfaction")
public class CustomerSatisfaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Quan hệ ManyToOne với User (khách hàng)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ ManyToOne với Appointment (cuộc hẹn đã sử dụng dịch vụ)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(nullable = false)
    private Integer rating;  // Điểm đánh giá 1 - 5

    @Column(columnDefinition = "TEXT")
    private String feedback; // Phản hồi thêm (có thể để trống)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructor mặc định
    public CustomerSatisfaction() {
        this.createdAt = LocalDateTime.now();
    }

    // Getter & Setter

}
