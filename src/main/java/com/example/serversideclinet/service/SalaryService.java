package com.example.serversideclinet.service;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.model.StoreService;
import com.example.serversideclinet.repository.AppointmentRepository;
import com.example.serversideclinet.repository.PayrollSummaryRepository;
import com.example.serversideclinet.repository.SalaryRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set; // Import Set
import java.util.HashSet; // Import HashSet

@Service
@Transactional
public class SalaryService {

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

    @Autowired
    private PayrollSummaryRepository payrollSummaryRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Tính lương cho một cuộc hẹn hoàn thành
     */
    public SalaryRecord calculateSalaryForAppointment(Appointment appointment) {
        if (!appointment.canCalculateSalary()) {
            throw new IllegalStateException("Appointment cannot be used for salary calculation: status=" + appointment.getStatus() + ", calculated=" + appointment.isSalaryCalculated());
        }

        Employee employee = appointment.getEmployee();
        StoreService storeService = appointment.getStoreService();

        // Thêm kiểm tra null cho employee và storeService
        if (employee == null) {
            throw new IllegalStateException("Employee is null for appointment " + appointment.getAppointmentId());
        }
        if (storeService == null) {
            throw new IllegalStateException("StoreService is null for appointment " + appointment.getAppointmentId());
        }

        BigDecimal servicePrice = storeService.getPrice();

        // Đảm bảo getCompletedAtLocal() không trả về null
        if (appointment.getCompletedAtLocal() == null) {
            throw new IllegalStateException("Completed date (completedAtLocal) is null for appointment " + appointment.getAppointmentId() + ". Ensure appointment was truly completed and saved.");
        }

        // Tính hoa hồng
        BigDecimal commissionRate = employee.getCommissionRate();
        BigDecimal commissionAmount = servicePrice.multiply(commissionRate)
                .setScale(2, RoundingMode.HALF_UP);

        // Tạo salary record
        SalaryRecord salaryRecord = new SalaryRecord();
        salaryRecord.setEmployee(employee);
        salaryRecord.setAppointment(appointment);
        salaryRecord.setServiceAmount(servicePrice);
        salaryRecord.setCommissionAmount(commissionAmount);
        salaryRecord.setCommissionRate(commissionRate);
        salaryRecord.setWorkDate(appointment.getCompletedAtLocal().toLocalDate());
        salaryRecord.setPaymentStatus(SalaryRecord.PaymentStatus.PENDING);

        // Lưu salary record
        salaryRecord = salaryRecordRepository.save(salaryRecord);

        // Đánh dấu appointment đã tính lương
        appointment.setSalaryCalculated(true);
        appointmentRepository.save(appointment);

        return salaryRecord;
    }

    /**
     * Tạo hoặc cập nhật bảng lương tổng kết cho nhân viên trong một khoảng thời gian
     */
    public PayrollSummary generatePayrollSummary(Employee employee, LocalDate startDate, LocalDate endDate) {
        // 1. Tìm PayrollSummary hiện có cho nhân viên và kỳ lương này
        // Sử dụng findByEmployeeAndPeriodStartDateBetween để tìm kiếm trong khoảng, sau đó lọc chính xác
        Optional<PayrollSummary> existingPayrollOpt = payrollSummaryRepository.findByEmployeeAndPeriodStartDateBetween(employee, startDate, endDate)
                .stream()
                .filter(p -> p.getPeriodStartDate().equals(startDate) && p.getPeriodEndDate().equals(endDate))
                .findFirst();

        // Lấy tất cả salary records trong khoảng thời gian
        List<SalaryRecord> salaryRecords = salaryRecordRepository
                .findByEmployeeAndWorkDateBetween(employee, startDate, endDate);

        // Tính toán tổng kết
        BigDecimal totalCommission = salaryRecords.stream()
                .map(SalaryRecord::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRevenue = salaryRecords.stream()
                .map(SalaryRecord::getServiceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalAppointments = salaryRecords.size();

        // Tính lương cơ bản
        BigDecimal baseSalary = calculateBaseSalary(employee, startDate, endDate);
        BigDecimal totalAmount = baseSalary.add(totalCommission);

        PayrollSummary payrollSummary;
        if (existingPayrollOpt.isPresent()) {
            payrollSummary = existingPayrollOpt.get();
            // Cập nhật các trường
            payrollSummary.setBaseSalary(baseSalary);
            payrollSummary.setTotalCommission(totalCommission);
            payrollSummary.setTotalAmount(totalAmount);
            payrollSummary.setTotalAppointments(totalAppointments);
            payrollSummary.setTotalRevenue(totalRevenue);
            // Giữ nguyên trạng thái nếu đã được APPROVED hoặc PAID
            if (payrollSummary.getStatus() == PayrollSummary.PayrollStatus.DRAFT || payrollSummary.getStatus() == PayrollSummary.PayrollStatus.PENDING) {
                // Nếu đang là DRAFT hoặc PENDING, có thể cập nhật lại DRAFT để admin review
                payrollSummary.setStatus(PayrollSummary.PayrollStatus.DRAFT);
            }
        } else {
            // Tạo payroll summary mới
            payrollSummary = new PayrollSummary();
            payrollSummary.setEmployee(employee);
            payrollSummary.setPeriodStartDate(startDate);
            payrollSummary.setPeriodEndDate(endDate);
            payrollSummary.setBaseSalary(baseSalary);
            payrollSummary.setTotalCommission(totalCommission);
            payrollSummary.setTotalAmount(totalAmount);
            payrollSummary.setTotalAppointments(totalAppointments);
            payrollSummary.setTotalRevenue(totalRevenue);
            payrollSummary.setStatus(PayrollSummary.PayrollStatus.DRAFT);
        }
        return payrollSummaryRepository.save(payrollSummary);
    }

    /**
     * Tính lương cơ bản dựa trên loại lương của nhân viên
     */
    private BigDecimal calculateBaseSalary(Employee employee, LocalDate startDate, LocalDate endDate) {
        Employee.SalaryType salaryType = employee.getSalaryType();
        BigDecimal baseSalary = employee.getBaseSalary();

        switch (salaryType) {
            case FIXED:
            case MIXED:
                // Nếu tính cho cả tháng (từ đầu tháng đến cuối tháng)
                if (startDate.getDayOfMonth() == 1 && endDate.equals(startDate.withDayOfMonth(startDate.lengthOfMonth()))) {
                    return baseSalary; // Trả full lương tháng
                } else {
                    // Tính theo tỷ lệ ngày thực tế trong khoảng thời gian được chọn
                    // Giả định một tháng có 30 ngày để đơn giản hóa việc chia tỷ lệ
                    long totalDaysInMonth = startDate.lengthOfMonth(); // Số ngày trong tháng của startDate
                    long workingDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;

                    if (totalDaysInMonth == 0) return BigDecimal.ZERO; // Tránh chia cho 0

                    return baseSalary.multiply(BigDecimal.valueOf(workingDays))
                            .divide(BigDecimal.valueOf(totalDaysInMonth), 2, RoundingMode.HALF_UP);
                }

            case COMMISSION:
                return BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * Tự động tính lương cho tất cả appointments hoàn thành chưa được tính
     * Và tự động cập nhật bảng lương tổng kết cho các nhân viên liên quan.
     */
    public void processUnprocessedAppointments() {
        List<Appointment> unprocessedAppointments = appointmentRepository
                .findByStatusAndSalaryCalculated(Appointment.Status.COMPLETED, false);

        // Dùng một Set để lưu các nhân viên cần tạo/cập nhật bảng lương
        Set<Employee> employeesToUpdatePayroll = new HashSet<>();

        for (Appointment appointment : unprocessedAppointments) {
            try {
                calculateSalaryForAppointment(appointment);
                // Thêm nhân viên vào danh sách cần cập nhật bảng lương
                employeesToUpdatePayroll.add(appointment.getEmployee());
            } catch (Exception e) {
                // Log lỗi nhưng tiếp tục xử lý các appointment khác
                System.err.println("Error calculating salary for appointment " +
                        appointment.getAppointmentId() + ": " + e.getMessage());
            }
        }

        // Sau khi xử lý tất cả các appointment, tạo/cập nhật payroll summary cho từng nhân viên
        LocalDate today = LocalDate.now();
        LocalDate startOfCurrentMonth = today.withDayOfMonth(1);
        LocalDate endOfCurrentMonth = today.withDayOfMonth(today.lengthOfMonth());

        for (Employee employee : employeesToUpdatePayroll) {
            try {
                // Tự động tạo/cập nhật payroll summary cho tháng hiện tại
                generatePayrollSummary(employee, startOfCurrentMonth, endOfCurrentMonth);
                System.out.println("Generated/Updated payroll summary for employee: " + employee.getEmail() + " for month: " + startOfCurrentMonth.getMonthValue());
            } catch (Exception e) {
                System.err.println("Error generating payroll summary for employee " +
                        employee.getEmail() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Duyệt bảng lương
     */
    public PayrollSummary approvePayroll(Integer payrollId, Employee approver) {
        PayrollSummary payroll = payrollSummaryRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
        payroll.setStatus(PayrollSummary.PayrollStatus.APPROVED);
        payroll.setApprovedBy(approver);
        payroll.setApprovedAt(LocalDateTime.now());

        return payrollSummaryRepository.save(payroll);
    }

    /**
     * Đánh dấu đã trả lương
     */
    public PayrollSummary markAsPaid(Integer payrollId) {
        PayrollSummary payroll = payrollSummaryRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
        if (payroll.getStatus() != PayrollSummary.PayrollStatus.APPROVED) {
            throw new IllegalStateException("Payroll must be approved before payment");
        }

        payroll.setStatus(PayrollSummary.PayrollStatus.PAID);
        payroll.setPaidAt(LocalDateTime.now());
        // Cập nhật trạng thái các salary records liên quan
        List<SalaryRecord> salaryRecords = salaryRecordRepository
                .findByEmployeeAndWorkDateBetween(payroll.getEmployee(),
                        payroll.getPeriodStartDate(), payroll.getPeriodEndDate());
        salaryRecords.forEach(record -> {
            record.setPaymentStatus(SalaryRecord.PaymentStatus.PAID);
            record.setPaidAt(LocalDateTime.now());
        });
        salaryRecordRepository.saveAll(salaryRecords);

        return payrollSummaryRepository.save(payroll);
    }
}