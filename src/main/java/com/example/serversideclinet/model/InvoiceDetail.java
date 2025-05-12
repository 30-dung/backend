package com.example.serversideclinet.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import jakarta.persistence.*;

import java.math.BigDecimal;

// InvoiceDetail.java
@Entity
@Table(name = "InvoiceDetail")
public class InvoiceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer detailId;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonBackReference("invoice-details")  // Thêm annotation này
    private Invoice invoice;

    @OneToOne
    @JoinColumn(name = "appointment_id")
    @JsonIdentityReference(alwaysAsId = true)  // Chỉ sử dụng ID cho appointment
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonIdentityReference(alwaysAsId = true)  // Chỉ sử dụng ID cho employee
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "store_service_id")
    @JsonIdentityReference(alwaysAsId = true)  // Chỉ sử dụng ID cho storeService
    private StoreService storeService;

    private String description;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    private Integer quantity = 1;

    // Getters and Setters

    public Integer getDetailId() {
        return detailId;
    }

    public void setDetailId(Integer detailId) {
        this.detailId = detailId;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
