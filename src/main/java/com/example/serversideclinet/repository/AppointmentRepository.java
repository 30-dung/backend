package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Find appointments for an employee within a specific time range
    List<Appointment> findByEmployeeAndStartTimeBetween(
            Employee employee, LocalDateTime startTime, LocalDateTime endTime);
    List<Appointment> findByUser(User user);
    List<Appointment> findByEmployeeAndStatus(Employee employee, Appointment.Status status);
}