package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByEmployee_EmployeeId(int employeeId);
}
