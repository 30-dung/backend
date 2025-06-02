package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.service.SalaryService;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.PayrollSummaryRepository;
import com.example.serversideclinet.repository.SalaryRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Lấy chi tiết bảng lương theo ngày
     */
    @GetMapping("/payroll/daily")
    public ResponseEntity<Map<String, Object>> getDailySalaryRecords(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) Integer employeeId) {

        try {
            List<SalaryRecord> salaryRecords;
            if (employeeId != null) {
                Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
                if (!employeeOpt.isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Không tìm thấy nhân viên");
                    return ResponseEntity.badRequest().body(response);
                }
                salaryRecords = salaryRecordRepository.findByEmployeeAndWorkDate(employeeOpt.get(), date);
            } else {
                salaryRecords = salaryRecordRepository.findByWorkDate(date);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("salaryRecords", salaryRecords);
            response.put("date", date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            response.put("totalRecords", salaryRecords.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi lấy dữ liệu: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy báo cáo lương theo khoảng thời gian
     */
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> getSalaryReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer employeeId) {

        try {
            List<SalaryRecord> salaryRecords;
            if (employeeId != null) {
                Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
                if (!employeeOpt.isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Không tìm thấy nhân viên");
                    return ResponseEntity.badRequest().body(response);
                }
                salaryRecords = salaryRecordRepository.findByEmployeeAndWorkDateBetween(
                        employeeOpt.get(), startDate, endDate);
            } else {
                salaryRecords = salaryRecordRepository.findByWorkDateBetween(startDate, endDate);
            }

            // Tính toán thống kê
            Map<String, Object> statistics = calculateStatistics(salaryRecords);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("salaryRecords", salaryRecords);
            response.put("statistics", statistics);
            response.put("startDate", startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            response.put("endDate", endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi tạo báo cáo: " + e.getMessage());

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
     * Tính toán thống kê từ danh sách salary records
     */
    private Map<String, Object> calculateStatistics(List<SalaryRecord> salaryRecords) {
        Map<String, Object> stats = new HashMap<>();

        if (salaryRecords.isEmpty()) {
            stats.put("totalRecords", 0);
            stats.put("totalCommission", 0);
            stats.put("totalRevenue", 0);
            stats.put("averageCommission", 0);
            return stats;
        }

        double totalCommission = salaryRecords.stream()
                .mapToDouble(record -> record.getCommissionAmount().doubleValue())
                .sum();

        double totalRevenue = salaryRecords.stream()
                .mapToDouble(record -> record.getServiceAmount().doubleValue())
                .sum();

        stats.put("totalRecords", salaryRecords.size());
        stats.put("totalCommission", totalCommission);
        stats.put("totalRevenue", totalRevenue);
        stats.put("averageCommission", totalCommission / salaryRecords.size());

        return stats;
    }
}