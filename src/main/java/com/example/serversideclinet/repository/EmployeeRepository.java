package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    // THÊM PHƯƠNG THỨC NÀY VÀO ĐÂY
    List<Employee> findByStoreStoreId(Integer storeId);
    // KẾT THÚC THÊM PHƯƠNG THỨC
}