package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.model.*;
import com.example.serversideclinet.model.StoreService;
import com.example.serversideclinet.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
        if (start == null) {
            throw new AppointmentException("Thời gian bắt đầu không được để trống.");
        }

        // Lấy thời gian kết thúc dựa trên dịch vụ
        StoreService storeService = storeServiceRepository.findById(request.getStoreServiceId())
                .orElseThrow(() -> new AppointmentException("Không tìm thấy dịch vụ."));

        int serviceDurationMinutes = storeService.getService().getDurationMinutes(); // Lấy thời gian dịch vụ từ StoreService
        LocalDateTime end = start.plusMinutes(serviceDurationMinutes); // Tính thời gian kết thúc

        // Kiểm tra thời gian hợp lệ
        validateAppointmentTime(start, end, timeSlot);

        // Kiểm tra sự khả dụng của thời gian slot
        if (!timeSlot.checkAvailability(start, end)) {
            throw new AppointmentException("Khung giờ này không khả dụng.");
        }

        // Tạo AppointmentTimeSlot mới trong WorkingTimeSlot
        AppointmentTimeSlot appointmentTimeSlot = timeSlot.createAppointmentSlot(start, end);
        if (appointmentTimeSlot == null) {
            throw new AppointmentException("Không thể tạo slot mới vì đã có xung đột thời gian.");
        }

        // Lấy employee
        Employee employee = timeSlot.getEmployee();

        // Tạo Appointment và liên kết với AppointmentTimeSlot mới
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setStoreService(storeService);
        appointment.setEmployee(employee);
        appointment.setStatus(Appointment.Status.PENDING);
        appointment.setNotes(request.getNotes());

        // Thiết lập mối quan hệ hai chiều
        appointment.getAppointmentTimeSlots().add(appointmentTimeSlot);
        appointmentTimeSlot.setAppointment(appointment);

        // Lưu AppointmentTimeSlot trước
        appointmentTimeSlot = appointmentTimeSlotRepository.save(appointmentTimeSlot);

        // Lưu Appointment sau
        appointment = appointmentRepository.save(appointment);

        // Gọi hàm tạo hoặc cập nhật hóa đơn
        createOrUpdateInvoice(user, appointment, employee, storeService);

        // Trả về Appointment trực tiếp
        return appointment;
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

    // Trả về danh sách tất cả các cuộc hẹn
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // Trả về danh sách các cuộc hẹn chưa được xác nhận (Pending)
    public List<Appointment> getAllPendingAppointments() {
        return appointmentRepository.findByStatus(Appointment.Status.PENDING);
    }

    public class AppointmentException extends RuntimeException {
        public AppointmentException(String message) {
            super(message);
        }
    }
}
