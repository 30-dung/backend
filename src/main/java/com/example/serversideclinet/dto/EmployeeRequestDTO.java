// Trong EmployeeRequestDTO.java

package com.example.serversideclinet.dto;

import com.example.serversideclinet.model.Employee; // Import Employee để sử dụng SalaryType
import com.example.serversideclinet.model.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.math.BigDecimal; // Import BigDecimal
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class EmployeeRequestDTO {
    @NotBlank
    private String employeeCode;
    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String password;
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    private Gender gender;
    private LocalDate dateOfBirth;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")


    private String specialization;

    private Integer storeId;

    private Set<Integer> roleIds;

    private String avatarUrl;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.00", message = "Base salary must be non-negative")
    private BigDecimal baseSalary;

    @NotNull(message = "Commission rate is required")
    @DecimalMin(value = "0.0000", message = "Commission rate must be non-negative")
    @DecimalMax(value = "1.0000", message = "Commission rate cannot exceed 1.0000 (100%)")
    private BigDecimal commissionRate;

    @NotNull(message = "Salary type is required")
    private Employee.SalaryType salaryType;

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Set<Integer> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Integer> roleIds) {
        this.roleIds = roleIds;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }


    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public Employee.SalaryType getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(Employee.SalaryType salaryType) {
        this.salaryType = salaryType;
    }
}