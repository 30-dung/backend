package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.AppointmentTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentTimeSlotRepository extends JpaRepository<AppointmentTimeSlot,Long> {
}
