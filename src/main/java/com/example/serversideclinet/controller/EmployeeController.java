package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.EmployeeRequestDTO;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới tạo được
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        Employee created = employeeService.createEmployee(requestDTO);
        return ResponseEntity.ok(created);
    }

//    @GetMapping("/appointments")
//    @PreAuthorize("hasRole('EMPLOYEE')")
//    public ResponseEntity<List<Appointment>> getOwnAppointments(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
//        String email = principal.getUsername();
//        List<Appointment> appointments = employeeService.getAppointmentsForLoggedInEmployee(email);
//        return ResponseEntity.ok(appointments);
//    }
//
//    @PutMapping("/appointments/{appointmentId}/status")
//    @PreAuthorize("hasRole('EMPLOYEE')")
//    public ResponseEntity<Appointment> updateOwnAppointmentStatus(@PathVariable Integer appointmentId,
//                                                                  @RequestParam Appointment.Status status,
//                                                                  @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
//        String email = principal.getUsername();
//        Appointment updated = employeeService.updateStatusByEmployee(email, appointmentId, status);
//        return ResponseEntity.ok(updated);
//    }

}
