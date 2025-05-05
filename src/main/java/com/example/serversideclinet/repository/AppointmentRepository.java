package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByUserAndStatus(User user, Appointment.Status status);
}
