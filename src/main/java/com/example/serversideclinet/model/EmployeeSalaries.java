package com.example.serversideclinet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private Integer salaryMonth;

    @Column(nullable = false)
    private Integer salaryPeriod;

    @CreationTimestamp
    private LocalDateTime calculatedAt;

    // Getters and Setters
    public Integer getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(Integer salaryId) {
        this.salaryId = salaryId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public SalaryRules getSalaryRule() {
        return salaryRule;
    }

    public void setSalaryRule(SalaryRules salaryRule) {
        this.salaryRule = salaryRule;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getTotalSalary() {
        return totalSalary;
    }

    public void setTotalSalary(BigDecimal totalSalary) {
        this.totalSalary = totalSalary;
    }

    public Integer getSalaryMonth() {
        return salaryMonth;
    }

    public void setSalaryMonth(Integer salaryMonth) {
        this.salaryMonth = salaryMonth;
    }

    public Integer getSalaryPeriod() {
        return salaryPeriod;
    }

    public void setSalaryPeriod(Integer salaryPeriod) {
        this.salaryPeriod = salaryPeriod;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public void setEmployeeId(Integer employeeId) {
        if (employeeId != null) {
            Employee emp = new Employee();
            emp.setEmployeeId(employeeId);
            this.employee = emp;
        }
    }

    public void setRuleId(Integer ruleId) {
        if (ruleId != null) {
            SalaryRules rule = new SalaryRules();
            rule.setRuleId(ruleId);
            this.salaryRule = rule;
        }
    }
}