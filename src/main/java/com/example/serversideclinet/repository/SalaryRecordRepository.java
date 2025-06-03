package com.example.serversideclinet.repository;
// Thêm vào SalaryRecordRepository.java

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Integer> {

    // Các method hiện tại...
    List<SalaryRecord> findByEmployeeAndWorkDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
}