// InvoiceService.java
package com.example.serversideclinet.service;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository;

    @Transactional
    public Invoice createInvoiceFromPendingAppointments(User user) {
        List<Appointment> appointments = appointmentRepository.findByUserAndStatus(user, Appointment.Status.PENDING);

        if (appointments.isEmpty()) {
            throw new RuntimeException("No pending appointments to create invoice.");
        }

        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setStatus(InvoiceStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (Appointment appointment : appointments) {
            InvoiceDetail detail = new InvoiceDetail();
            detail.setAppointment(appointment);
            detail.setEmployee(appointment.getEmployee());
            detail.setStoreService(appointment.getStoreService());
            detail.setUnitPrice(appointment.getStoreService().getPrice());
            detail.setQuantity(1);
            detail.setDescription("Appointment #" + appointment.getAppointmentId());
            detail.setInvoice(invoice);

            total = total.add(detail.getUnitPrice());
            invoice.getInvoiceDetails().add(detail);
        }

        invoice.setTotalAmount(total);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice markInvoiceAsPaid(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setStatus(InvoiceStatus.PAID);

        // ✅ Update trạng thái các Appointment liên quan
        for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
            Appointment appointment = detail.getAppointment();
            appointment.setStatus(Appointment.Status.CONFIRMED);
            appointmentRepository.save(appointment);
        }

        return invoiceRepository.save(invoice);
    }

    public List<Invoice> findUnpaidInvoicesByUser(User user) {
        return invoiceRepository.findByUserAndStatus(user, InvoiceStatus.PENDING);
    }
}
