package com.example.serversideclinet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// WorkingTimeSlot.java
@Entity
@Table(name = "WorkingTimeSlot")
public class WorkingTimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer timeSlotId;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private Boolean isAvailable = true;

    // Getters and Setters
}
