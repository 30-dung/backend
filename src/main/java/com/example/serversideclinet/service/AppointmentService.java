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

        if (!timeSlot.checkAvailability(start, end)) {
            throw new AppointmentException("Khung giờ này không khả dụng.");
        }

        // Tạo AppointmentTimeSlot mới trong WorkingTimeSlot
        AppointmentTimeSlot appointmentTimeSlot = timeSlot.createAppointmentSlot(start, end);
        if (appointmentTimeSlot == null) {
            throw new AppointmentException("Không thể tạo slot mới vì đã có xung đột thời gian.");
        }

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

        // Đánh dấu slot đã được dùng
        workingTimeSlotRepository.save(timeSlot);

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

        return appointment;
    }

    public class AppointmentException extends RuntimeException {
        public AppointmentException(String message) {
            super(message);
        }
    }


    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}


