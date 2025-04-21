package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByEmail(String email);
//    List<Appointment> findByTimeSlot_Employee_EmployeeId(Integer employeeId);
}
