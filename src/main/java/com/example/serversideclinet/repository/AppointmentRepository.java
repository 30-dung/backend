package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <-- Dòng này phải có
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer>, JpaSpecificationExecutor<Appointment> { // <-- Đảm bảo dòng này chính xác

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByUserOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeAndStatus(Employee employee, Appointment.Status status);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeAndStartTimeBetween(Employee employee, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.employee = :employee " +
            "AND a.status != 'CANCELED' AND a.status != 'REJECTED' " +
            "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeAndTimeOverlap(@Param("employee") Employee employee,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    Optional<Appointment> findBySlug(String slug);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeOrderByCreatedAtDesc(Employee employee);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeAndStatusNotIn(Employee employee, List<Appointment.Status> statuses);

    List<Appointment> findByStatusAndSalaryCalculated(Appointment.Status status, boolean b);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByStatus(Appointment.Status status);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeAndStatusAndStartTimeBetween(Employee employee, Appointment.Status status, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByStatusAndStartTimeBetween(Appointment.Status status, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeOrderByStartTimeDesc(Employee employee);

    @Override
    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findAll();
}