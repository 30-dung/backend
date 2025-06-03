package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// Thêm vào SalaryRecordRepository.java
@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Integer> {

    // Methods hiện tại...
    List<SalaryRecord> findByEmployeeAndWorkDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);

    // Methods mới có thể cần:
    List<SalaryRecord> findByEmployeeOrderByWorkDateDesc(Employee employee);

    List<SalaryRecord> findByEmployeeAndPaymentStatus(Employee employee, SalaryRecord.PaymentStatus status);
}