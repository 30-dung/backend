package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.AppointmentStatusResponseDTO;
import com.example.serversideclinet.dto.EmployeeRequestDTO;
import com.example.serversideclinet.dto.PendingAppointmentDTO;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.service.AppointmentService;
import com.example.serversideclinet.service.EmployeeService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<Employee>> getEmployeesByStore(@PathVariable Integer storeId) {
        List<Employee> employees = employeeService.getEmployeesByStore(storeId);
        return ResponseEntity.ok(employees);
    }

    @PostMapping("create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        Employee created = employeeService.createEmployee(requestDTO);
        return ResponseEntity.ok(created);
    }
    @GetMapping("pending")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<PendingAppointmentDTO>> getPendingAppointmentsForEmployee(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Integer employeeId = currentUser.getEmployeeId();
        List<PendingAppointmentDTO> pendingAppointments = employeeService.getPendingAppointmentsForEmployee(employeeId);

        return ResponseEntity.ok(pendingAppointments);
    }
    @PutMapping("appointments/{appointmentId}/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AppointmentStatusResponseDTO> updateAppointmentStatus(
            @PathVariable Integer appointmentId,
            @RequestParam("action") String action,
            @AuthenticationPrincipal CustomUserDetails currentUser) throws MessagingException, IOException {

        Integer employeeId = currentUser.getEmployeeId();

        AppointmentStatusResponseDTO responseDTO = employeeService.updateAppointmentStatus(employeeId, appointmentId, action);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('EMPLOYEE')") // Chỉ nhân viên được truy cập endpoint này
    public ResponseEntity<Employee> getEmployeeProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        Integer employeeId = currentUser.getEmployeeId();
        Employee employee = employeeService.findEmployeeById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin của nhân viên với ID: " + employeeId));

        return ResponseEntity.ok(employee);
    }

}

