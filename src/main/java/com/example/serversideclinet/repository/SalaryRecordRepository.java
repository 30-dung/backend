
// SalaryRecordRepository.java
package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.SalaryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Integer> {

    // Existing methods
    List<SalaryRecord> findByEmployeeAndWorkDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
    List<SalaryRecord> findByEmployeeOrderByWorkDateDesc(Employee employee);
    List<SalaryRecord> findByEmployeeAndPaymentStatus(Employee employee, SalaryRecord.PaymentStatus status);

    // Additional query methods
    Optional<SalaryRecord> findByAppointment(Appointment appointment);

    boolean existsByAppointment(Appointment appointment);

    // Pagination
    Page<SalaryRecord> findByEmployee(Employee employee, Pageable pageable);

    Page<SalaryRecord> findByEmployeeAndPaymentStatus(Employee employee,
                                                      SalaryRecord.PaymentStatus status,
                                                      Pageable pageable);

    // Statistics queries
    @Query("SELECT SUM(sr.commissionAmount) FROM SalaryRecord sr WHERE sr.employee = :employee " +
            "AND sr.workDate BETWEEN :startDate AND :endDate AND sr.paymentStatus = :status")
    BigDecimal sumCommissionByEmployeeAndPeriodAndStatus(@Param("employee") Employee employee,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate,
                                                         @Param("status") SalaryRecord.PaymentStatus status);

    @Query("SELECT COUNT(sr) FROM SalaryRecord sr WHERE sr.employee = :employee " +
            "AND sr.workDate BETWEEN :startDate AND :endDate")
    Long countByEmployeeAndPeriod(@Param("employee") Employee employee,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(sr.serviceAmount) FROM SalaryRecord sr WHERE sr.employee = :employee " +
            "AND sr.workDate BETWEEN :startDate AND :endDate")
    BigDecimal sumServiceAmountByEmployeeAndPeriod(@Param("employee") Employee employee,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    // Tìm records chưa được xử lý
    List<SalaryRecord> findByPaymentStatus(SalaryRecord.PaymentStatus status);

    // Tìm records theo tháng
    @Query("SELECT sr FROM SalaryRecord sr WHERE YEAR(sr.workDate) = :year AND MONTH(sr.workDate) = :month")
    List<SalaryRecord> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    // Tìm records theo employee và tháng
    @Query("SELECT sr FROM SalaryRecord sr WHERE sr.employee = :employee " +
            "AND YEAR(sr.workDate) = :year AND MONTH(sr.workDate) = :month")
    List<SalaryRecord> findByEmployeeAndYearAndMonth(@Param("employee") Employee employee,
                                                     @Param("year") int year,
                                                     @Param("month") int month);
}