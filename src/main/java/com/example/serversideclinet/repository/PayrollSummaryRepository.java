// PayrollSummaryRepository.java
package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.PayrollSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollSummaryRepository extends JpaRepository<PayrollSummary, Integer> {

    // Existing methods
    List<PayrollSummary> findByEmployeeAndPeriodStartDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
    List<PayrollSummary> findByPeriodStartDateBetween(LocalDate startDate, LocalDate endDate);

    // Pagination methods
    Page<PayrollSummary> findByEmployee(Employee employee, Pageable pageable);
    // Additional query methods
    List<PayrollSummary> findByEmployeeAndStatus(Employee employee, PayrollSummary.PayrollStatus status);

    List<PayrollSummary> findByStatus(PayrollSummary.PayrollStatus status);

    // Tìm payroll theo employee và overlap với period
    // Phương thức này có thể không cần thiết nếu bạn lọc chính xác bằng periodStartDate và periodEndDate
    // Nhưng giữ lại nếu có các trường hợp kỳ lương có thể chồng lấn mà bạn muốn phát hiện
    @Query("SELECT p FROM PayrollSummary p WHERE p.employee = :employee " +
            "AND ((p.periodStartDate <= :endDate AND p.periodEndDate >= :startDate))")
    List<PayrollSummary> findOverlappingPayrolls(@Param("employee") Employee employee,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
    // Tìm payroll gần nhất của employee
    Optional<PayrollSummary> findTopByEmployeeOrderByPeriodEndDateDesc(Employee employee);
    // Statistics queries
    @Query("SELECT COUNT(p) FROM PayrollSummary p WHERE p.status = :status")
    Long countByStatus(@Param("status") PayrollSummary.PayrollStatus status);
    @Query("SELECT SUM(p.totalAmount) FROM PayrollSummary p WHERE p.status = :status " +
            "AND p.periodStartDate >= :startDate AND p.periodEndDate <= :endDate")
    java.math.BigDecimal sumTotalAmountByStatusAndPeriod(@Param("status") PayrollSummary.PayrollStatus status,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
}