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
        // Lấy user
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lấy time slot
        WorkingTimeSlot timeSlot = workingTimeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        if (!timeSlot.getIsAvailable()) {
            throw new RuntimeException("Time slot is already booked");
        }

        // Lấy employee và store service
        Employee employee = timeSlot.getEmployee();
        StoreService storeService = storeServiceRepository.findById(request.getStoreServiceId())
                .orElseThrow(() -> new RuntimeException("StoreService not found"));

        // Tạo appointment
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setTimeSlot(timeSlot);
        appointment.setStoreService(storeService);
        appointment.setEmployee(employee);
        appointment.setStatus(Appointment.Status.PENDING);
        appointment.setNotes(request.getNotes());
        appointment = appointmentRepository.save(appointment);

        // Đánh dấu slot đã được dùng
        timeSlot.setIsAvailable(false);
        workingTimeSlotRepository.save(timeSlot);

        // Tạo hóa đơn nếu user chưa có invoice nào PENDING
        Invoice invoice = invoiceRepository
                .findFirstByUserAndStatus(user, InvoiceStatus.PENDING)
                .orElseGet(() -> {
                    Invoice newInvoice = new Invoice();
                    newInvoice.setUser(user);
                    newInvoice.setStatus(InvoiceStatus.PENDING);
                    newInvoice.setCreatedAt(LocalDateTime.now());
                    return invoiceRepository.save(newInvoice);
                });

        // Tính toán tổng giá trị hóa đơn
        BigDecimal totalAmount = invoice.getInvoiceDetails().stream()
                .map(InvoiceDetail::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tạo invoice detail cho dịch vụ vừa đặt
        InvoiceDetail invoiceDetail = new InvoiceDetail();
        invoiceDetail.setInvoice(invoice);
        invoiceDetail.setAppointment(appointment);
        invoiceDetail.setEmployee(employee);
        invoiceDetail.setStoreService(storeService);
        invoiceDetail.setUnitPrice(storeService.getPrice());
        invoiceDetail.setQuantity(1);
        invoiceDetail.setDescription(storeService.getService().getServiceName());
        invoiceDetailRepository.save(invoiceDetail);

        // Cập nhật tổng giá trị của hóa đơn
        totalAmount = totalAmount.add(invoiceDetail.getUnitPrice());  // Cộng đơn giá vào tổng giá trị
        invoice.setTotalAmount(totalAmount);  // Cập nhật totalAmount
        invoiceRepository.save(invoice);  // Lưu hóa đơn với tổng giá trị cập nhật

        return appointment;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}


