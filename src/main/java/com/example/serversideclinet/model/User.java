package com.example.serversideclinet.model;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// User.java
@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private MembershipType membershipType = MembershipType.REGULAR;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    private Integer loyaltyPoints = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Getters and Setters
}

