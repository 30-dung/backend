package com.example.serversideclinet.model;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
// Appointment.java
@Entity
@Table(name = "Appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer appointmentId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "time_slot_id", unique = true, nullable = false)
    private WorkingTimeSlot timeSlot;

    @ManyToOne
    @JoinColumn(name = "store_service_id", nullable = false)
    private StoreService storeService;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private String notes;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    private Boolean reminderSent = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Getters and Setters
}