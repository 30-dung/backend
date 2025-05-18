package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.EmployeeSalaries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;


public interface EmployeeSalariesRepository extends JpaRepository<EmployeeSalaries, Long> {
    /**
     * Tìm bản ghi lương mới nhất của nhân viên trong khoảng thời gian
     */
    Optional<EmployeeSalaries> findTopByEmployeeAndCalculatedAtBetweenOrderByCalculatedAtDesc(
            Employee employee,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );
}
