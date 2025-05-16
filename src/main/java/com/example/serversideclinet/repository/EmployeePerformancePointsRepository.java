package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.EmployeePerformancePoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePerformancePointsRepository extends JpaRepository<EmployeePerformancePoints, Integer> {

    /**
     * Tìm bản ghi điểm thưởng của nhân viên trong một tháng cụ thể
     *
     * @param employee Nhân viên
     * @param yearMonth Tháng (định dạng YYYY-MM)
     * @return Optional của bản ghi điểm thưởng
     */
    Optional<EmployeePerformancePoints> findByEmployeeAndYearMonth(Employee employee, String yearMonth);

    /**
     * Tìm tất cả bản ghi điểm thưởng trong một tháng cụ thể
     *
     * @param yearMonth Tháng (định dạng YYYY-MM)
     * @return Danh sách bản ghi điểm thưởng
     */
    List<EmployeePerformancePoints> findByYearMonth(String yearMonth);

    /**
     * Tìm tất cả bản ghi điểm thưởng của một nhân viên
     *
     * @param employee Nhân viên
     * @return Danh sách bản ghi điểm thưởng
     */
    List<EmployeePerformancePoints> findByEmployee(Employee employee);

    /**
     * Tìm tất cả bản ghi điểm thưởng chưa được xử lý
     *
     * @return Danh sách bản ghi điểm thưởng
     */
    List<EmployeePerformancePoints> findByIsProcessed(Boolean isProcessed);

    /**
     * Tìm tất cả bản ghi điểm thưởng chưa được xử lý trong một tháng cụ thể
     *
     * @param yearMonth Tháng (định dạng YYYY-MM)
     * @param isProcessed Trạng thái xử lý
     * @return Danh sách bản ghi điểm thưởng
     */
    List<EmployeePerformancePoints> findByYearMonthAndIsProcessed(String yearMonth, Boolean isProcessed);
}