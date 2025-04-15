package com.example.serversideclinet.model;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
// SalaryRules.java
@Entity
@Table(name = "SalaryRules")
public class SalaryRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ruleId;

    @Column(nullable = false)
    private String description;

    private BigDecimal baseSalary;

    private BigDecimal bonusPerAppointment;

    @Column(precision = 5, scale = 2)
    private BigDecimal bonusPercentage;

    @Column(nullable = false)
    private LocalDateTime effectiveDate;

    private Boolean isActive = true;

    // Getters and Setters
}