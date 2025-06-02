package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.PayrollSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollSummaryRepository extends JpaRepository<PayrollSummary,Integer> {
}
