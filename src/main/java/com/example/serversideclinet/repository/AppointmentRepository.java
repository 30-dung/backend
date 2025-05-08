package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Trả về tất cả các cuộc hẹn
    List<Appointment> findAll();

    // Trả về các cuộc hẹn có trạng thái PENDING
    List<Appointment> findByStatus(Appointment.Status status);

    // Trả về cuộc hẹn theo ID
    Optional<Appointment> findById(Integer id);


}
