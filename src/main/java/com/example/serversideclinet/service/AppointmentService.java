package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.model.StoreService;
import com.example.serversideclinet.model.WorkingTimeSlot;
import com.example.serversideclinet.repository.AppointmentRepository;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.StoreServiceRepository;
import com.example.serversideclinet.repository.WorkingTimeSlotRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StoreServiceRepository storeServiceRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Appointment createAppointment(AppointmentRequest request, String userEmail) {
        // Tìm người dùng theo email
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lấy khung giờ làm việc
        WorkingTimeSlot timeSlot = workingTimeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        // Kiểm tra khung giờ hợp lệ
        if (!timeSlot.getIsAvailable()) {
            throw new RuntimeException("Time slot is already booked");
        }

        Employee employee = timeSlot.getEmployee();
        if (employee == null) {
            throw new RuntimeException("Employee not assigned to slot");
        }

        // Lấy dịch vụ cụ thể của cửa hàng
        StoreService storeService = storeServiceRepository.findById(request.getStoreServiceId())
                .orElseThrow(() -> new RuntimeException("StoreService not found"));

        // Tạo appointment
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setTimeSlot(timeSlot);
        appointment.setStoreService(storeService);
        appointment.setEmployee(employee); // ✅ Thêm dòng này để tránh lỗi
        appointment.setStatus(Appointment.Status.PENDING);
        appointment.setNotes(request.getNotes());

        // Đánh dấu timeSlot là đã được đặt
        timeSlot.setIsAvailable(false);
        workingTimeSlotRepository.save(timeSlot);

        // Lưu appointment
        return appointmentRepository.save(appointment);
    }



    // Các phương thức còn lại
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment getAppointmentById(int id) {
        return appointmentRepository.findById(id).orElse(null);
    }

    @Transactional
    public Appointment updateAppointmentStatus(int id, Appointment.Status status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }
}

