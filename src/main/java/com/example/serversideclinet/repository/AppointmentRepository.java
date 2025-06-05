package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByUserOrderByCreatedAtDesc(User user); // Sửa đổi để sắp xếp theo createdAt giảm dần

    List<Appointment> findByEmployeeAndStatus(Employee employee, Appointment.Status status);

    List<Appointment> findByEmployeeAndStartTimeBetween(Employee employee, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.employee = :employee " +
            "AND a.status != 'CANCELED' " +
            "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee"})
    List<Appointment> findByEmployeeAndTimeOverlap(@Param("employee") Employee employee,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    Optional<Appointment> findBySlug(String slug);
}