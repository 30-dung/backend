package com.example.serversideclinet.service;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.EmployeePerformancePointsRepository;
import com.example.serversideclinet.repository.EmployeeSalariesRepository;
import com.example.serversideclinet.repository.PerformanceBonusConfigRepository;
import com.example.serversideclinet.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalaryCalculationService {

    @Autowired
    private EmployeeSalariesRepository employeeSalariesRepository;

    @Autowired
    private EmployeePerformancePointsRepository performancePointsRepository;

    @Autowired
    private PerformanceBonusConfigRepository bonusConfigRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private static final BigDecimal POINT_VALUE = new BigDecimal("1000"); // 1 điểm = 1000 VND

    /**
     * Tính toán lương cho nhân viên cho ngày hiện tại
     *
     * @param employee Nhân viên cần tính lương
     * @param baseSalary Lương cơ bản của nhân viên
     * @return Thông tin lương đã được tính toán
     */
    @Transactional
    public EmployeeSalaries calculateDailySalary(Employee employee, BigDecimal baseSalary) {
        // Lấy ngày hiện tại
        LocalDate today = LocalDate.now();
        String yearMonth = today.getYear() + "-" + String.format("%02d", today.getMonthValue());

        // Tìm thông tin điểm thưởng của nhân viên trong tháng hiện tại
        EmployeePerformancePoints performancePoints = performancePointsRepository
                .findByEmployeeAndYearMonth(employee, yearMonth)
                .orElseGet(() -> new EmployeePerformancePoints(employee, yearMonth));

        // Lương cơ bản hàng ngày = lương cơ bản / 30
        BigDecimal dailyBaseSalary = baseSalary.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);

        // Tính toán tiền thưởng dựa trên điểm (1 điểm = 1000 VND)
        BigDecimal bonus = new BigDecimal(performancePoints.getTotalPoints()).multiply(POINT_VALUE);

        // Tổng lương = lương cơ bản hàng ngày + tiền thưởng
        BigDecimal totalSalary = dailyBaseSalary.add(bonus);

        // Tạo bản ghi lương mới
        EmployeeSalaries salary = new EmployeeSalaries();
        salary.setEmployee(employee);
        salary.setBaseSalary(dailyBaseSalary);
        salary.setBonus(bonus);
        salary.setTotalSalary(totalSalary);

        // Lưu thông tin ngày tháng hiện tại
        YearMonth ym = YearMonth.parse(yearMonth);
        salary.setSalaryMonth(ym.getMonthValue());
        salary.setSalaryPeriod(ym.getYear());
        salary.setCalculatedAt(LocalDateTime.now());

        // Lưu thông tin lương
        return employeeSalariesRepository.save(salary);
    }

    /**
     * Tính toán lương cho tất cả nhân viên cho ngày hiện tại
     * Phương thức này được gọi bởi Admin
     *
     * @return Danh sách thông tin lương đã được tính toán
     */
    @Transactional
    public List<EmployeeSalaries> calculateDailySalariesForAllEmployees() {
        List<Employee> allEmployees = employeeRepository.findAll();
        List<EmployeeSalaries> result = new ArrayList<>();

        for (Employee employee : allEmployees) {
            // Giả sử có phương thức để lấy lương cơ bản của nhân viên
            BigDecimal baseSalary = getEmployeeBaseSalary(employee);

            EmployeeSalaries salary = calculateDailySalary(employee, baseSalary);
            result.add(salary);
        }

        return result;
    }

    /**
     * Phương thức lấy lương cơ bản của nhân viên
     * Cần triển khai dựa trên cấu trúc dữ liệu của bạn
     */
    private BigDecimal getEmployeeBaseSalary(Employee employee) {
        // Thay thế bằng logic lấy lương cơ bản của nhân viên từ cơ sở dữ liệu
        // Ví dụ: return employee.getBaseSalary();

        // Tạm thời trả về 9,000,000 VND cho ví dụ
        return new BigDecimal("9000000");
    }

    /**
     * Lấy thông tin lương của nhân viên cho ngày hiện tại
     */
    public EmployeeSalaries getEmployeeDailySalary(Employee employee) {
        LocalDate today = LocalDate.now();
        // Lấy thông tin lương gần nhất được tính trong ngày hôm nay
        return employeeSalariesRepository.findTopByEmployeeAndCalculatedAtBetweenOrderByCalculatedAtDesc(
                employee,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        ).orElse(null);
    }
}