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

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getBonusPerAppointment() {
        return bonusPerAppointment;
    }

    public void setBonusPerAppointment(BigDecimal bonusPerAppointment) {
        this.bonusPerAppointment = bonusPerAppointment;
    }

    public BigDecimal getBonusPercentage() {
        return bonusPercentage;
    }

    public void setBonusPercentage(BigDecimal bonusPercentage) {
        this.bonusPercentage = bonusPercentage;
    }

    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}