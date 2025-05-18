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
     * Xử lý đánh giá nhận được từ controller
     */
    @Transactional
    public CustomerSatisfaction processRating(Integer appointmentId, Integer rating, String feedback) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("Appointment ID must not be null");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Lấy email của người dùng từ JWT
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));

        // Kiểm tra quyền sở hữu
        if (!appointment.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Appointment does not belong to the current user");
        }

        // Kiểm tra appointment đã hoàn thành chưa
        if (appointment.getStatus() != Appointment.Status.COMPLETED) {
            throw new RuntimeException("Only completed appointments can be rated");
        }


        // Kiểm tra đã đánh giá chưa
        if (customerSatisfactionRepository.existsByAppointment(appointment)) {
            throw new RuntimeException("This appointment has already been rated");
        }

        // Kiểm tra thông tin nhân viên có tồn tại
        if (appointment.getEmployee() == null) {
            throw new RuntimeException("Cannot rate appointment without assigned employee");
        }

        // Tạo và lưu CustomerSatisfaction
        CustomerSatisfaction satisfaction = new CustomerSatisfaction();
        satisfaction.setRating(rating);
        satisfaction.setFeedback(feedback);
        satisfaction.setAppointment(appointment);
        satisfaction.setUser(user);
        satisfaction.setCreatedAt(LocalDateTime.now());

        CustomerSatisfaction savedSatisfaction = customerSatisfactionRepository.save(satisfaction);

        // Cập nhật điểm thưởng
        updateEmployeePerformancePoints(savedSatisfaction);

        // Đánh dấu đã nhắc nhở để không gửi lại
        appointment.setReminderSent(true);
        appointmentRepository.save(appointment);

        return savedSatisfaction;
    }

    /**
     * Cập nhật điểm thưởng cho nhân viên dựa trên đánh giá
     */
    private void updateEmployeePerformancePoints(CustomerSatisfaction satisfaction) {
        Employee employee = satisfaction.getAppointment().getEmployee();

        if (employee == null) {
            throw new RuntimeException("Employee information missing from appointment");
        }

        Integer rating = satisfaction.getRating();
        Integer points = calculatePointsFromRating(rating);

        if (points > 0) {
            String currentYearMonth = EmployeePerformancePoints.getCurrentYearMonthString();

            EmployeePerformancePoints performancePoints =
                    employeePerformancePointsRepository.findByEmployeeAndYearMonth(employee, currentYearMonth)
                            .orElseGet(() -> new EmployeePerformancePoints(employee, currentYearMonth));

            performancePoints.addPoints(points);
            employeePerformancePointsRepository.save(performancePoints);
        }
    }

    /**
     * Tính điểm thưởng dựa trên rating
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
