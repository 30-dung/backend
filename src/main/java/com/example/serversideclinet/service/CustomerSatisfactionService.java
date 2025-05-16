package com.example.serversideclinet.service;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.CustomerSatisfactionRepository;
import com.example.serversideclinet.repository.EmployeePerformancePointsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CustomerSatisfactionService {

    @Autowired
    private CustomerSatisfactionRepository customerSatisfactionRepository;

    @Autowired
    private EmployeePerformancePointsRepository employeePerformancePointsRepository;

    /**
     * Lưu đánh giá của khách hàng và cập nhật điểm thưởng cho nhân viên
     *
     * @param customerSatisfaction thông tin đánh giá của khách hàng
     * @return đánh giá đã được lưu
     */
    @Transactional
    public CustomerSatisfaction saveCustomerSatisfaction(CustomerSatisfaction customerSatisfaction) {
        // Đảm bảo thời gian tạo là thời gian hiện tại
        customerSatisfaction.setCreatedAt(LocalDateTime.now());

        // Lưu thông tin đánh giá
        CustomerSatisfaction savedSatisfaction = customerSatisfactionRepository.save(customerSatisfaction);

        // Cập nhật điểm thưởng cho nhân viên dựa trên đánh giá
        updateEmployeePerformancePoints(savedSatisfaction);

        return savedSatisfaction;
    }

    /**
     * Cập nhật điểm thưởng cho nhân viên dựa trên đánh giá của khách hàng
     *
     * @param satisfaction đánh giá của khách hàng
     */
    private void updateEmployeePerformancePoints(CustomerSatisfaction satisfaction) {
        // Lấy thông tin nhân viên từ cuộc hẹn
        Employee employee = satisfaction.getAppointment().getEmployee();

        // Lấy thông tin đánh giá
        Integer rating = satisfaction.getRating();

        // Tính điểm thưởng dựa trên đánh giá
        Integer points = calculatePointsFromRating(rating);

        if (points > 0) {
            // Lấy tháng hiện tại để lưu điểm thưởng
            String currentYearMonth = EmployeePerformancePoints.getCurrentYearMonthString();

            // Tìm hoặc tạo mới bản ghi điểm thưởng cho nhân viên trong tháng này
            EmployeePerformancePoints performancePoints =
                    employeePerformancePointsRepository.findByEmployeeAndYearMonth(employee, currentYearMonth)
                            .orElseGet(() -> new EmployeePerformancePoints(employee, currentYearMonth));

            // Cộng điểm thưởng
            performancePoints.addPoints(points);

            // Lưu vào cơ sở dữ liệu
            employeePerformancePointsRepository.save(performancePoints);
        }
    }

    /**
     * Tính điểm thưởng dựa trên đánh giá
     *
     * @param rating đánh giá (1-5 sao)
     * @return số điểm thưởng
     */
    private Integer calculatePointsFromRating(Integer rating) {
        // Quy tắc tính điểm:
        // 4 sao: +5 điểm
        // 5 sao: +10 điểm
        switch (rating) {
            case 5:
                return 10;
            case 4:
                return 5;
            default:
                return 0; // Không có điểm thưởng cho đánh giá dưới 4 sao
        }
    }
}