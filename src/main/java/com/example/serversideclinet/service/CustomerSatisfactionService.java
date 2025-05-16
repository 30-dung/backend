package com.example.serversideclinet.service;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.AppointmentRepository;
import com.example.serversideclinet.repository.CustomerSatisfactionRepository;
import com.example.serversideclinet.repository.EmployeePerformancePointsRepository;
import com.example.serversideclinet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CustomerSatisfactionService {

    @Autowired
    private CustomerSatisfactionRepository customerSatisfactionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmployeePerformancePointsRepository employeePerformancePointsRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lưu đánh giá của khách hàng và cập nhật điểm thưởng cho nhân viên
     *
     * @param customerSatisfaction thông tin đánh giá của khách hàng
     * @return đánh giá đã được lưu
     */
    @Transactional
    public CustomerSatisfaction saveCustomerSatisfaction(CustomerSatisfaction customerSatisfaction) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        customerSatisfaction.setUser(user);
        customerSatisfaction.setCreatedAt(LocalDateTime.now());

        // Load lại appointment đầy đủ để đảm bảo employee không null
        Appointment appointment = appointmentRepository.findById(customerSatisfaction.getAppointment().getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        customerSatisfaction.setAppointment(appointment);

        CustomerSatisfaction savedSatisfaction = customerSatisfactionRepository.save(customerSatisfaction);

        updateEmployeePerformancePoints(savedSatisfaction);

        appointment.setReminderSent(true);
        appointmentRepository.save(appointment);

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
        switch (rating) {
            case 5:
                return 10;
            case 4:
                return 5;
            default:
                return 0;
        }
    }
}
