package com.example.serversideclinet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.serversideclinet.model.EmployeePerformancePoints;
import com.example.serversideclinet.service.SalaryCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableScheduling
public class SalarySchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SalarySchedulerConfig.class);

    @Autowired
    private SalaryCalculationService salaryCalculationService;

    /**
     * Lịch trình tự động tính toán lương vào cuối tháng
     * Chạy vào 23:59:59 của ngày cuối cùng mỗi tháng
     */
    @Scheduled(cron = "59 59 23 L * ?") // L = ngày cuối cùng của tháng
    public void calculateSalariesAtEndOfMonth() {
        String currentYearMonth = EmployeePerformancePoints.getCurrentYearMonthString();
        logger.info("Bắt đầu tính toán lương tự động cho tháng {}", currentYearMonth);

        try {
            salaryCalculationService.calculateSalariesForAllEmployees(currentYearMonth);
            logger.info("Hoàn thành tính toán lương tự động cho tháng {}", currentYearMonth);
        } catch (Exception e) {
            logger.error("Lỗi khi tính toán lương tự động: {}", e.getMessage());
        }
    }
}