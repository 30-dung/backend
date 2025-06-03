package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.PayrollSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // ✅ ĐÚNG - Spring Data Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayrollSummaryRepository extends JpaRepository<PayrollSummary, Integer> {

    // Methods hiện tại...
    List<PayrollSummary> findByEmployeeAndPeriodStartDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
    List<PayrollSummary> findByPeriodStartDateBetween(LocalDate startDate, LocalDate endDate);

    // Method cho pagination
    Page<PayrollSummary> findByEmployee(Employee employee, Pageable pageable);
}