package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.AppointmentStatusResponseDTO;
import com.example.serversideclinet.dto.EmployeeRequestDTO;
import com.example.serversideclinet.dto.EmployeeProfileUpdateDTO; // Import
import com.example.serversideclinet.dto.PasswordUpdateDTO; // Import
import com.example.serversideclinet.dto.PendingAppointmentDTO;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.service.AppointmentService;
import com.example.serversideclinet.service.EmployeeService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        List<EmployeeResponse> responses = employees.stream()
                .map(e -> new EmployeeResponse(e.getEmployeeId(), e.getFullName(), e.getEmail()))
                .toList();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    // Endpoint để nhân viên cập nhật thông tin cá nhân
    @PutMapping("/update-profile")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Employee> updateEmployeeProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody EmployeeProfileUpdateDTO updateDTO) {
        Integer employeeId = currentUser.getEmployeeId();
        Employee updatedEmployee = employeeService.updateEmployeeProfile(employeeId, updateDTO);
        return ResponseEntity.ok(updatedEmployee);
    }

    // Endpoint để nhân viên cập nhật mật khẩu
    @PutMapping("/update-profile/password")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<String> updateEmployeePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        Integer employeeId = currentUser.getEmployeeId();
        employeeService.updateEmployeePassword(employeeId, passwordUpdateDTO);
        return ResponseEntity.ok("Cập nhật mật khẩu thành công.");
    }

    //cái này cho admin update nhơ thông tin của nv
    @PutMapping("/admin/update/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Employee> adminUpdateEmployee(
            @PathVariable Integer employeeId,
            @Valid @RequestBody EmployeeRequestDTO updateDTO) { // Sử dụng EmployeeRequestDTO
        Employee updatedEmployee = employeeService.adminUpdateEmployee(employeeId, updateDTO);
        return ResponseEntity.ok(updatedEmployee);
    }

    public static class EmployeeResponse {
        private Integer employeeId;
        private String fullName;
        private String email;

        public EmployeeResponse(Integer employeeId, String fullName, String email) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.email = email;
        }

        public Integer getEmployeeId() {
            return employeeId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }
    }
}