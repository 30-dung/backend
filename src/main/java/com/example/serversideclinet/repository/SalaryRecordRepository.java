package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Integer> {
    // Các method hiện tại...
    List<SalaryRecord> findByEmployeeAndWorkDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
    // Thêm method mới
    List<SalaryRecord> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);
    List<SalaryRecord> findByWorkDate(LocalDate workDate);
    List<SalaryRecord> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);
}