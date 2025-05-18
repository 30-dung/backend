package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.CustomerSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerSatisfactionRepository extends JpaRepository<CustomerSatisfaction,Integer> {
    boolean existsByAppointment(Appointment appointment);

}
