package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.EmployeeSalaries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeSalariesRepository extends JpaRepository<EmployeeSalaries, Integer> {
    List<EmployeeSalaries> findBySalaryMonthAndSalaryPeriod(int salaryMonth, int salaryPeriod);
}