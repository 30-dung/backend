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
    List<Appointment> findByUserOrderByCreatedAtDesc(User user);

    List<Appointment> findByEmployeeAndStatus(Employee employee, Appointment.Status status);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeAndStartTimeBetween(Employee employee, LocalDateTime startTime, LocalDateTime endTime); // Đã có

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

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeOrderByCreatedAtDesc(Employee employee);

    List<Appointment> findByStatusAndSalaryCalculated(Appointment.Status status, boolean b);

    List<Appointment> findByStatus(Appointment.Status status);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeAndStatusAndStartTimeBetween(Employee employee, Appointment.Status status, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByStatusAndStartTimeBetween(Appointment.Status status, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    List<Appointment> findByEmployeeOrderByStartTimeDesc(Employee employee); // Dùng cho trường hợp chỉ lọc theo nhân viên, sắp xếp theo start time

    // Giữ nguyên các query findByFilters cho ADMIN, không thay đổi
    @Query("SELECT a FROM Appointment a " +
            "WHERE (:status IS NULL OR a.status = :status) " +
            "AND (:employeeEmail IS NULL OR a.employee.email = :employeeEmail) " +
            "AND (:startDate IS NULL OR a.startTime >= :startDate) " +
            "AND (:endDate IS NULL OR a.startTime <= :endDate)")
    List<Appointment> findByFilters(
            @Param("status") String status,
            @Param("employeeEmail") String employeeEmail,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT a FROM Appointment a " +
            "WHERE (:status IS NULL OR a.status = :status) " +
            "AND (:employeeEmail IS NULL OR a.employee.email = :employeeEmail)")
    List<Appointment> findByFilters(
            @Param("status") String status,
            @Param("employeeEmail") String employeeEmail
    );

}