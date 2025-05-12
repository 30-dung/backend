package com.example.serversideclinet.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "WorkingTimeSlot")
public class WorkingTimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer timeSlotId;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private Boolean isAvailable = true;

    @OneToMany(mappedBy = "workingSlot", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("slot-appointments")
    private List<Appointment> appointments = new ArrayList<>();

    // Getters and Setters
    public Integer getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Integer timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean available) {
        isAvailable = available;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    // Helper methods
    public void addAppointment(Appointment appointment) {
        this.appointments.add(appointment);
        if (appointment.getWorkingSlot() != this) {
            appointment.setWorkingSlot(this);
        }
    }

    public void removeAppointment(Appointment appointment) {
        this.appointments.remove(appointment);
        if (appointment.getWorkingSlot() == this) {
            appointment.setWorkingSlot(null);
        }
    }
}