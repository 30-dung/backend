package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.AppointmentTimeSlot;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.model.Invoice;
import com.example.serversideclinet.model.InvoiceStatus;
import com.example.serversideclinet.model.StoreService;
import com.example.serversideclinet.model.InvoiceDetail;
import com.example.serversideclinet.model.WorkingTimeSlot;
import com.example.serversideclinet.repository.*;
import com.example.serversideclinet.model.Employee;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    @Autowired
    private AppointmentTimeSlotRepository appointmentTimeSlotRepository;

    @Autowired
    private StoreServiceRepository storeServiceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository;

    @Transactional
    public Appointment createAppointment(AppointmentRequest request, String userEmail) {
        if (request.getTimeSlotId() == null) {
            throw new AppointmentException("Time slot ID không được để trống.");
        }

        if (request.getStoreServiceId() == null) {
            throw new AppointmentException("StoreService ID không được để trống.");
        }

        // Lấy user
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new AppointmentException("Không tìm thấy người dùng."));

        // Lấy WorkingTimeSlot và kiểm tra khả dụng
        WorkingTimeSlot timeSlot = workingTimeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new AppointmentException("Không tìm thấy khung giờ làm việc."));

        // Kiểm tra sự khả dụng của slot cho khoảng thời gian yêu cầu
        LocalDateTime start = request.getStartTime(); // Lấy startTime từ request
        LocalDateTime end = request.getEndTime(); // Lấy endTime từ request

        // Kiểm tra thời gian hợp lệ
        validateAppointmentTime(start, end, timeSlot);

        if (!timeSlot.checkAvailability(start, end)) {
            throw new AppointmentException("Khung giờ này không khả dụng.");
        }

        // Tạo AppointmentTimeSlot mới trong WorkingTimeSlot
        AppointmentTimeSlot appointmentTimeSlot = timeSlot.createAppointmentSlot(start, end);
        if (appointmentTimeSlot == null) {
            throw new AppointmentException("Không thể tạo slot mới vì đã có xung đột thời gian.");
        }

        // Lưu AppointmentTimeSlot trước
        appointmentTimeSlot = appointmentTimeSlotRepository.save(appointmentTimeSlot);

        // Lấy employee và store service
        Employee employee = timeSlot.getEmployee();
        StoreService storeService = storeServiceRepository.findById(request.getStoreServiceId())
                .orElseThrow(() -> new AppointmentException("Không tìm thấy dịch vụ."));

        // Tạo Appointment và liên kết với AppointmentTimeSlot mới
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setStoreService(storeService);
        appointment.setEmployee(employee);
        appointment.setAppointmentTimeSlot(appointmentTimeSlot); // Liên kết với AppointmentTimeSlot
        appointment.setStatus(Appointment.Status.PENDING);
        appointment.setNotes(request.getNotes());
        appointment = appointmentRepository.save(appointment);

        // Thiết lập mối quan hệ hai chiều
        appointmentTimeSlot.setAppointment(appointment);
        appointmentTimeSlotRepository.save(appointmentTimeSlot);

        // Tạo hóa đơn nếu chưa có
        createOrUpdateInvoice(user, appointment, employee, storeService);

        return appointment;
    }

    private void validateAppointmentTime(LocalDateTime start, LocalDateTime end, WorkingTimeSlot timeSlot) {
        if (start == null || end == null) {
            throw new AppointmentException("Thời gian bắt đầu và kết thúc không được để trống.");
        }

        if (!start.isBefore(end)) {
            throw new AppointmentException("Thời gian bắt đầu phải trước thời gian kết thúc.");
        }

        // Kiểm tra thời gian đặt lịch nằm trong khoảng thời gian làm việc
        if (start.isBefore(timeSlot.getStartTime()) || end.isAfter(timeSlot.getEndTime())) {
            throw new AppointmentException("Thời gian đặt lịch phải nằm trong khung giờ làm việc của nhân viên.");
        }
    }

    private void createOrUpdateInvoice(User user, Appointment appointment, Employee employee, StoreService storeService) {
        // Tạo hóa đơn nếu chưa có
        Invoice invoice = invoiceRepository
                .findFirstByUserAndStatus(user, InvoiceStatus.PENDING)
                .orElseGet(() -> {
                    Invoice newInvoice = new Invoice();
                    newInvoice.setUser(user);
                    newInvoice.setStatus(InvoiceStatus.PENDING);
                    newInvoice.setCreatedAt(LocalDateTime.now());
                    return invoiceRepository.save(newInvoice);
                });

        // Tổng tiền cũ
        BigDecimal totalAmount = invoice.getInvoiceDetails().stream()
                .map(InvoiceDetail::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Chi tiết hóa đơn
        InvoiceDetail invoiceDetail = new InvoiceDetail();
        invoiceDetail.setInvoice(invoice);
        invoiceDetail.setAppointment(appointment);
        invoiceDetail.setEmployee(employee);
        invoiceDetail.setStoreService(storeService);
        invoiceDetail.setUnitPrice(storeService.getPrice());
        invoiceDetail.setQuantity(1);
        invoiceDetail.setDescription(storeService.getService().getServiceName());
        invoiceDetailRepository.save(invoiceDetail);

        // Cập nhật lại tổng tiền
        totalAmount = totalAmount.add(invoiceDetail.getUnitPrice());
        invoice.setTotalAmount(totalAmount);
        invoiceRepository.save(invoice);
    }

    @Transactional
    public Appointment updateAppointmentStatus(Integer appointmentId, Appointment.Status newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        appointment.setStatus(newStatus);

        // Nếu hủy lịch hẹn, giải phóng AppointmentTimeSlot
        if (newStatus == Appointment.Status.CANCELED) {
            AppointmentTimeSlot timeSlot = appointment.getAppointmentTimeSlot();
            if (timeSlot != null) {
                timeSlot.setIsBooked(false);
                appointmentTimeSlotRepository.save(timeSlot);
            }
        }

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByUser(String userEmail) {
        Optional<User> userOptional = userService.findByEmail(userEmail);
        if (!userOptional.isPresent()) {
            throw new AppointmentException("Không tìm thấy người dùng.");
        }

        return appointmentRepository.findByUserOrderByCreatedAtDesc(userOptional.get());
    }

    public List<Appointment> getAppointmentsByEmployee(String employeeEmail) {
        // Cần thêm EmployeeService để tìm kiếm employee theo email
        // Employee employee = employeeService.findByEmail(employeeEmail)...

        // Giả định đã có phương thức tìm kiếm appointments theo employee
        // return appointmentRepository.findByEmployeeOrderByCreatedAtDesc(employee);

        throw new UnsupportedOperationException("Chức năng này chưa được triển khai.");
    }

    public class AppointmentException extends RuntimeException {
        public AppointmentException(String message) {
            super(message);
        }
    }
}