package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.service.SalaryService;
import com.example.serversideclinet.service.EmployeeService;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.PayrollSummaryRepository;
import com.example.serversideclinet.repository.SalaryRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/salary")
@CrossOrigin(origins = "*")
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollSummaryRepository payrollSummaryRepository;

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

    /**
     * Cập nhật thông tin lương cho nhân viên
     */
    @PutMapping("/employee/{employeeId}/salary")
    public ResponseEntity<Map<String, Object>> updateEmployeeSalary(
            @PathVariable Integer employeeId,
            @RequestParam(required = false) BigDecimal baseSalary,
            @RequestParam(required = false) BigDecimal commissionRate,
            @RequestParam(required = false) String salaryType) {

        try {
            Employee.SalaryType type = null;
            if (salaryType != null) {
                type = Employee.SalaryType.valueOf(salaryType.toUpperCase());
            }

            Employee employee = employeeService.updateEmployeeSalary(employeeId, baseSalary, commissionRate, type);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật thông tin lương thành công");
            response.put("employee", employee);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi cập nhật lương: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật lương cho nhân viên hiện tại (có thể dùng SQL để update nhanh)
     */
    @PostMapping("/update-existing-employees")
    public ResponseEntity<Map<String, Object>> updateExistingEmployeesSalary() {
        try {
            List<Employee> employees = employeeRepository.findAll();
            int updatedCount = 0;

            for (Employee employee : employees) {
                // Chỉ update những nhân viên chưa có lương được set
                if (employee.getBaseSalary().compareTo(BigDecimal.ZERO) == 0 &&
                        employee.getCommissionRate().compareTo(BigDecimal.ZERO) == 0) {

                    employee.setBaseSalary(new BigDecimal("10000000.00")); // 10 triệu
                    employee.setCommissionRate(new BigDecimal("0.05"));     // 5%
                    employee.setSalaryType(Employee.SalaryType.MIXED);

                    employeeRepository.save(employee);
                    updatedCount++;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã cập nhật lương cho " + updatedCount + " nhân viên");
            response.put("updatedCount", updatedCount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi cập nhật: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Tự động tính lương cho tất cả appointments chưa được tính
     */
    @PostMapping("/process-unprocessed")
    public ResponseEntity<Map<String, Object>> processUnprocessedAppointments() {
        try {
            salaryService.processUnprocessedAppointments();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã xử lý tất cả appointments chưa được tính lương");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi xử lý: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Tạo bảng lương cho nhân viên trong khoảng thời gian
     */
    @PostMapping("/generate-payroll")
    public ResponseEntity<Map<String, Object>> generatePayroll(
            @RequestParam Integer employeeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (!employeeOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy nhân viên");
                return ResponseEntity.badRequest().body(response);
            }

            Employee employee = employeeOpt.get();
            PayrollSummary payrollSummary = salaryService.generatePayrollSummary(employee, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo bảng lương thành công");
            response.put("payrollSummary", payrollSummary);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi tạo bảng lương: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách bảng lương theo tháng
     */
    @GetMapping("/payroll/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyPayrolls(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Integer employeeId) {

        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            List<PayrollSummary> payrolls;
            if (employeeId != null) {
                Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
                if (!employeeOpt.isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Không tìm thấy nhân viên");
                    return ResponseEntity.badRequest().body(response);
                }
                payrolls = payrollSummaryRepository.findByEmployeeAndPeriodStartDateBetween(
                        employeeOpt.get(), startDate, endDate);
            } else {
                payrolls = payrollSummaryRepository.findByPeriodStartDateBetween(startDate, endDate);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payrolls", payrolls);
            response.put("period", String.format("%02d/%d", month, year));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi lấy dữ liệu: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Duyệt bảng lương
     */
    @PostMapping("/approve/{payrollId}")
    public ResponseEntity<Map<String, Object>> approvePayroll(
            @PathVariable Integer payrollId,
            @RequestParam Integer approverId) {

        try {
            Optional<Employee> approverOpt = employeeRepository.findById(approverId);
            if (!approverOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy người duyệt");
                return ResponseEntity.badRequest().body(response);
            }

            PayrollSummary payroll = salaryService.approvePayroll(payrollId, approverOpt.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Duyệt bảng lương thành công");
            response.put("payroll", payroll);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi duyệt bảng lương: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Đánh dấu đã trả lương
     */
    @PostMapping("/mark-paid/{payrollId}")
    public ResponseEntity<Map<String, Object>> markAsPaid(@PathVariable Integer payrollId) {
        try {
            PayrollSummary payroll = salaryService.markAsPaid(payrollId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đánh dấu đã trả lương thành công");
            response.put("payroll", payroll);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi cập nhật trạng thái: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách tất cả nhân viên (để chọn trong dropdown)
     */
    @GetMapping("/employees")
    public ResponseEntity<Map<String, Object>> getAllEmployees() {
        try {
            List<Employee> employees = employeeRepository.findAll();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("employees", employees);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi lấy danh sách nhân viên: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}