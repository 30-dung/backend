package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.service.SalaryService;
import com.example.serversideclinet.service.EmployeeService;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.PayrollSummaryRepository;
import com.example.serversideclinet.repository.SalaryRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollSummaryRepository payrollSummaryRepository;

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

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

    @GetMapping("/my-payroll")
    public ResponseEntity<Map<String, Object>> getMyPayrollByMonth(
            @RequestParam int year,
            @RequestParam int month) {

        try {
            // Lấy email từ token (SecurityContext)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy thông tin nhân viên"
                ));
            }

            Employee employee = employeeOpt.get();

            // Xác định khoảng thời gian của tháng
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            List<PayrollSummary> payrolls = payrollSummaryRepository.findByEmployeeAndPeriodStartDateBetween(
                    employee, startDate, endDate);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "employee", Map.of(
                            "employeeId", employee.getEmployeeId(),
                            "email", employee.getEmail()
                    ),
                    "payrolls", payrolls,
                    "month", month,
                    "year", year
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lấy bảng lương theo tháng: " + e.getMessage()
            ));
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

    /**
     * API lấy lịch sử bảng lương của nhân viên (dựa theo token)
     */
    @GetMapping("/my-payroll/history")
    public ResponseEntity<Map<String, Object>> getMyPayrollHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // Lấy email từ SecurityContext (token đã xử lý sẵn)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không tìm thấy thông tin nhân viên"
                ));
            }

            Employee employee = employeeOpt.get();

            // ✅ FIXED: Sử dụng đúng Pageable từ Spring Data
            Pageable pageable = PageRequest.of(page, size, Sort.by("periodStartDate").descending());
            Page<PayrollSummary> payrollPage = payrollSummaryRepository.findByEmployee(employee, pageable);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "employee", Map.of(
                            "employeeId", employee.getEmployeeId(),
                            "email", employee.getEmail()
                    ),
                    "payrolls", payrollPage.getContent(),
                    "totalRecords", payrollPage.getTotalElements(),
                    "totalPages", payrollPage.getTotalPages(),
                    "currentPage", payrollPage.getNumber(),
                    "hasNext", payrollPage.hasNext(),
                    "hasPrevious", payrollPage.hasPrevious()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lỗi lấy lịch sử bảng lương: " + e.getMessage()
            ));
        }
    }
}