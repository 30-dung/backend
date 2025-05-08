package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.CustomerRatingDTO;
import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.CustomerRatingRepository;
import com.example.serversideclinet.repository.AppointmentRepository;
import com.example.serversideclinet.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class CustomerRatingService {

    @Autowired
    private CustomerRatingRepository customerRatingRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public CustomerRatingDTO rateEmployee(Integer appointmentId, Integer stars, String comment) {
        // Lấy Appointment từ ID
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment không tồn tại"));

        // Kiểm tra trạng thái cuộc hẹn, chỉ cho phép đánh giá khi đã hoàn thành
        if (appointment.getStatus() != Appointment.Status.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể đánh giá khi cuộc hẹn đã hoàn thành.");
        }

        // Lấy nhân viên từ Appointment
        Employee employee = appointment.getEmployee();

        // Tạo mới CustomerRating
        CustomerRating customerRating = new CustomerRating();
        customerRating.setUser(appointment.getUser());
        customerRating.setEmployee(employee);
        customerRating.setAppointment(appointment);
        customerRating.setStars(stars);
        customerRating.setComment(comment);

        // Lưu đánh giá vào cơ sở dữ liệu
        customerRatingRepository.save(customerRating);

        // Cập nhật điểm KPI cho nhân viên
        updateEmployeeKpiPoints(employee, stars);

        // Cập nhật trạng thái cuộc hẹn thành đã hoàn thành
        appointment.setStatus(Appointment.Status.COMPLETED);
        appointmentRepository.save(appointment);

        // Chuyển đổi CustomerRating thành DTO để trả về
        return new CustomerRatingDTO(customerRating.getRatingId(), customerRating.getStars(), customerRating.getComment(), customerRating.getRatingDate());
    }

    private void updateEmployeeKpiPoints(Employee employee, Integer stars) {
        int additionalPoints = 0;

        if (stars == 5) {
            additionalPoints = 10;
        } else if (stars == 4) {
            additionalPoints = 5;
        }

        // Cập nhật điểm KPI cho nhân viên
        employee.setKpiPoints(employee.getKpiPoints() + additionalPoints);
        employeeRepository.save(employee);
    }
}
