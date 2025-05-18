package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.EmployeePerformancePoints;
import com.example.serversideclinet.model.EmployeeSalaries;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.service.SalaryCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/salaries")
public class SalaryController {


    @Autowired
    private SalaryCalculationService salaryCalculationService;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * API endpoint cho Admin để tính lương nhân viên ngày hiện tại
     */
    @PostMapping("/calculate-daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> calculateDailySalaries() {
        List<EmployeeSalaries> salaries = salaryCalculationService.calculateDailySalariesForAllEmployees();

        List<SalaryResponse> response = salaries.stream()
                .map(salary -> new SalaryResponse(
                        salary.getEmployee().getEmployeeId(),
                        salary.getEmployee().getFullName(),
                        salary.getBaseSalary(),
                        salary.getBonus(),
                        salary.getTotalSalary()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * API endpoint để Admin lấy thông tin lương của nhân viên trong ngày hiện tại
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEmployeeDailySalary(@PathVariable Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        EmployeeSalaries salary = salaryCalculationService.getEmployeeDailySalary(employee);

        if (salary == null) {
            return ResponseEntity.notFound().build();
        }

        SalaryResponse response = new SalaryResponse(
                ((Employee) employee).getEmployeeId(),
                employee.getFullName(),
                salary.getBaseSalary(),
                salary.getBonus(),
                salary.getTotalSalary()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Class để trả về dữ liệu lương
     */
    static class SalaryResponse {
        private Integer employeeId;
        private String employeeName;
        private java.math.BigDecimal dailyBaseSalary;
        private java.math.BigDecimal performanceBonus;
        private java.math.BigDecimal totalDailySalary;

        public SalaryResponse(Integer employeeId, String employeeName,
                              java.math.BigDecimal dailyBaseSalary,
                              java.math.BigDecimal performanceBonus,
                              java.math.BigDecimal totalDailySalary) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.dailyBaseSalary = dailyBaseSalary;
            this.performanceBonus = performanceBonus;
            this.totalDailySalary = totalDailySalary;
        }

        // Getters
        public Integer getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public java.math.BigDecimal getDailyBaseSalary() { return dailyBaseSalary; }
        public java.math.BigDecimal getPerformanceBonus() { return performanceBonus; }
        public java.math.BigDecimal getTotalDailySalary() { return totalDailySalary; }
    }
}