package com.example.serversideclinet.model;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
// Service.java
@Entity
@Table(name = "Service")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceId;

    @Column(nullable = false)
    private String serviceName;

    private String description;

    @Column(nullable = false)
    private Short durationMinutes;

    // Getters and Setters
}