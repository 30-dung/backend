package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.EmployeePerformancePoints;
import com.example.serversideclinet.model.EmployeeSalaries;
import com.example.serversideclinet.service.SalaryCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/salaries")
public class SalaryController {

    @Autowired
    private SalaryCalculationService salaryCalculationService;

    /**
     * Tính toán lương cho tất cả nhân viên trong tháng hiện tại
     *
     * @return Danh sách thông tin lương đã được tính toán
     */
    @PostMapping("/calculate-current-month")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<String> calculateSalariesForCurrentMonth() {
        String currentYearMonth = EmployeePerformancePoints.getCurrentYearMonthString();
        try {
            salaryCalculationService.calculateSalariesForAllEmployees(currentYearMonth);
            return ResponseEntity.ok("Đã tính toán lương thành công cho tháng " + currentYearMonth);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tính toán lương: " + e.getMessage());
        }
    }

    /**
     * Tính toán lương cho tất cả nhân viên trong một tháng cụ thể
     *
     * @param yearMonth Tháng cần tính lương (định dạng YYYY-MM)
     * @return Danh sách thông tin lương đã được tính toán
     */
    @PostMapping("/calculate/{yearMonth}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<String> calculateSalariesForMonth(@PathVariable @DateTimeFormat(pattern = "yyyy-MM") String yearMonth) {
        try {
            // Xác thực định dạng yearMonth
            YearMonth.parse(yearMonth);

            salaryCalculationService.calculateSalariesForAllEmployees(yearMonth);
            return ResponseEntity.ok("Đã tính toán lương thành công cho tháng " + yearMonth);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tính toán lương: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin lương của một nhân viên trong một tháng cụ thể
     *
     * @param employeeId ID của nhân viên
     * @param yearMonth Tháng (định dạng YYYY-MM)
     * @return Thông tin lương
     */
    @GetMapping("/employee/{employeeId}/{yearMonth}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @securityService.isCurrentEmployee(#employeeId)")
    public ResponseEntity<?> getEmployeeSalary(
            @PathVariable Integer employeeId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") String yearMonth) {

        // Sẽ triển khai sau khi có repository method phù hợp
        return ResponseEntity.ok().build();
    }
}