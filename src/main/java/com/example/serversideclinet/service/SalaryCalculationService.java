package com.example.serversideclinet.service;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.EmployeePerformancePointsRepository;
import com.example.serversideclinet.repository.EmployeeSalariesRepository;
import com.example.serversideclinet.repository.PerformanceBonusConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class SalaryCalculationService {

    @Autowired
    private EmployeeSalariesRepository employeeSalariesRepository;

    @Autowired
    private EmployeePerformancePointsRepository performancePointsRepository;

    @Autowired
    private PerformanceBonusConfigRepository bonusConfigRepository;

    /**
     * Tính toán lương cho nhân viên vào cuối tháng
     *
     * @param employee Nhân viên cần tính lương
     * @param yearMonth Tháng cần tính lương (định dạng YYYY-MM)
     * @param baseSalary Lương cơ bản của nhân viên
     * @return Thông tin lương đã được tính toán
     */
    @Transactional
    public EmployeeSalaries calculateSalary(Employee employee, String yearMonth, BigDecimal baseSalary) {
        // Tìm thông tin điểm thưởng của nhân viên trong tháng
        EmployeePerformancePoints performancePoints = performancePointsRepository
                .findByEmployeeAndYearMonth(employee, yearMonth)
                .orElseGet(() -> new EmployeePerformancePoints(employee, yearMonth));

        // Tính toán tiền thưởng dựa trên điểm
        BigDecimal bonus = calculateBonusFromPoints(baseSalary, performancePoints.getTotalPoints());

        // Tổng lương = lương cơ bản + tiền thưởng
        BigDecimal totalSalary = baseSalary.add(bonus);

        // Tạo bản ghi lương mới
        EmployeeSalaries salary = new EmployeeSalaries();
        salary.setEmployee(employee);
        salary.setBaseSalary(baseSalary);
        salary.setBonus(bonus);
        salary.setTotalSalary(totalSalary);

        // Parse yearMonth thành thông tin tháng và năm
        YearMonth ym = YearMonth.parse(yearMonth);
        salary.setSalaryMonth(ym.getMonthValue());
        salary.setSalaryPeriod(ym.getYear());

        salary.setCalculatedAt(LocalDateTime.now());

        // Lưu thông tin lương
        EmployeeSalaries savedSalary = employeeSalariesRepository.save(salary);

        // Đánh dấu điểm thưởng đã được xử lý
        performancePoints.setIsProcessed(true);
        performancePointsRepository.save(performancePoints);

        return savedSalary;
    }

    /**
     * Tính toán tiền thưởng dựa trên số điểm và lương cơ bản
     *
     * @param baseSalary Lương cơ bản
     * @param points Số điểm tích lũy
     * @return Số tiền thưởng
     */
    private BigDecimal calculateBonusFromPoints(BigDecimal baseSalary, Integer points) {
        // Lấy các cấu hình tiền thưởng từ cơ sở dữ liệu (sắp xếp theo ngưỡng điểm giảm dần)
        List<PerformanceBonusConfig> bonusConfigs = bonusConfigRepository.findAllByOrderByPointsThresholdDesc();

        // Nếu không có cấu hình, sử dụng cấu hình mặc định
        if (bonusConfigs.isEmpty()) {
            // Mặc định: 100 điểm = 1,000,000 VND (cố định)
            if (points >= 100) {
                return new BigDecimal("1000000");
            } else {
                // Tính theo tỷ lệ nếu dưới 100 điểm
                return new BigDecimal(points * 10000); // 10,000 VND mỗi điểm
            }
        }

        // Duyệt qua các cấu hình để tìm mức thưởng phù hợp
        for (PerformanceBonusConfig config : bonusConfigs) {
            if (points >= config.getPointsThreshold()) {
                return config.calculateBonus(baseSalary, points);
            }
        }

        // Không đạt ngưỡng điểm nào
        return BigDecimal.ZERO;
    }

    /**
     * Tính toán lương cho tất cả nhân viên vào cuối tháng
     *
     * @param yearMonth Tháng cần tính lương (định dạng YYYY-MM)
     * @return Danh sách thông tin lương đã được tính toán
     */
    @Transactional
    public List<EmployeeSalaries> calculateSalariesForAllEmployees(String yearMonth) {
        // Sẽ triển khai sau khi có phương thức để lấy tất cả nhân viên
        // và lương cơ bản của họ
        throw new UnsupportedOperationException("Method not implemented yet");
    }
}