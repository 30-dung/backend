package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.model.WorkingTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkingTimeSlotRepository extends JpaRepository<WorkingTimeSlot, Integer> {
    boolean existsByEmployeeAndStartTimeLessThanAndEndTimeGreaterThan(
            Employee employee, LocalDateTime endTime, LocalDateTime startTime);
    List<WorkingTimeSlot> findByEmployeeOrderByStartTimeAsc(Employee employee);

    List<WorkingTimeSlot> findByStoreAndIsActiveAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(Store store, boolean b, LocalDateTime fromDate, LocalDateTime toDate);
}
