package com.example.serversideclinet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;

// EmployeeSalaries.java
@Entity
@Table(name = "EmployeeSalaries")
public class EmployeeSalaries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer salaryId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    private SalaryRules salaryRule;

    @Column(nullable = false)
    private BigDecimal baseSalary;

    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalSalary;

    private Year salaryMonth;

    private Byte salaryPeriod;

    @CreationTimestamp
    private LocalDateTime calculatedAt;

    // Getters and Setters
}