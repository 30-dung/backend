package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.AppointmentTimeSlot;
import com.example.serversideclinet.model.WorkingTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentTimeSlotRepository extends JpaRepository<AppointmentTimeSlot,Integer> {
    List<AppointmentTimeSlot> findByStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndIsBookedFalse(LocalDateTime start, LocalDateTime end);

    void deleteByWorkingTimeSlotAndIsBookedFalse(WorkingTimeSlot workingTimeSlot);

    List<AppointmentTimeSlot> findByWorkingTimeSlotAndIsBookedFalse(WorkingTimeSlot workingTimeSlot);
}
