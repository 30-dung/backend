package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord,Integer> {
    List<SalaryRecord> findByEmployeeAndWorkDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
}
