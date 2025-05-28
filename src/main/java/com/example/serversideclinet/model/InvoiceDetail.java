package com.example.serversideclinet.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class InvoiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer invoiceDetailId;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    // Thêm nếu schema yêu cầu
    @Column(name = "employee_id")
    private Integer employeeId;

    @Column(name = "store_service_id")
    private Integer storeServiceId;

    // Getters and Setters
    public Integer getInvoiceDetailId() {
        return invoiceDetailId;
    }

    public void setInvoiceDetailId(Integer invoiceDetailId) {
        this.invoiceDetailId = invoiceDetailId;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getStoreServiceId() {
        return storeServiceId;
    }

    public void setStoreServiceId(Integer storeServiceId) {
        this.storeServiceId = storeServiceId;
    }
}