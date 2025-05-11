package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.SalaryRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaryRulesRepository extends JpaRepository<SalaryRules, Integer> {
    @Override
    Optional<SalaryRules> findById(Integer integer);
}
