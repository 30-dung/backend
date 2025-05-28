package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    @EntityGraph(attributePaths = {"storeService", "storeService.store", "storeService.service"})
    Optional<Appointment> findById(Integer id);

    List<Appointment> findByEmployeeAndStatus(Employee employee, Appointment.Status status);

    List<Appointment> findByEmployeeAndStartTimeBetween(Employee employee, LocalDateTime startTime, LocalDateTime endTime);

    List<Appointment> findByUser(User user);
}