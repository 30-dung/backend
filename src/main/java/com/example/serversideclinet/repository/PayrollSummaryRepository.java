package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.PayrollSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface PayrollSummaryRepository extends JpaRepository<PayrollSummary, Integer> {

    // Các method hiện tại...
    List<PayrollSummary> findByEmployeeAndPeriodStartDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
    List<PayrollSummary> findByPeriodStartDateBetween(LocalDate startDate, LocalDate endDate);

    // Thêm method mới cho employee endpoints
    List<PayrollSummary> findByEmployeeOrderByPeriodStartDateDesc(Employee employee);
}