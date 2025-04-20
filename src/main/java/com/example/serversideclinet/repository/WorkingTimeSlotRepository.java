package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.WorkingTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkingTimeSlotRepository extends JpaRepository<WorkingTimeSlot, Integer> {
    List<WorkingTimeSlot> findByEmployee_EmployeeIdAndIsAvailableTrue(Integer employeeId);
}
