package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.EmployeeRequestDTO;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.service.AppointmentService;
import com.example.serversideclinet.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        Employee created = employeeService.createEmployee(requestDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<Appointment>> getAppointmentsByEmployee(
            @AuthenticationPrincipal CustomUserDetails employeeDetails) {
        int employeeId = employeeDetails.getEmployeeId();
        List<Appointment> appointments = appointmentService.getAppointmentsByEmployeeId(employeeId);
        return ResponseEntity.ok(appointments);
    }

    @PutMapping("/appointments/{id}/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Appointment> updateAppointmentStatus(
            @PathVariable("id") int appointmentId,
            @RequestParam("status") String newStatus,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int employeeId = userDetails.getEmployeeId();
        Appointment updated = appointmentService.updateStatusByEmployee(appointmentId, newStatus, employeeId);
        return ResponseEntity.ok(updated);
    }
}
