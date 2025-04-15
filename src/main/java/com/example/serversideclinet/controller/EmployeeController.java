package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.EmployeeRequestDTO;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới tạo được
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        Employee created = employeeService.createEmployee(requestDTO);
        return ResponseEntity.ok(created);
    }
}
