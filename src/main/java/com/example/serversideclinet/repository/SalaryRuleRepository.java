package com.example.serversideclinet.repository;
import com.example.serversideclinet.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SalaryRuleRepository extends JpaRepository<Appointment, Integer> {

}
