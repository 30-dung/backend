package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.AppointmentStatusResponseDTO;
import com.example.serversideclinet.dto.EmployeeRequestDTO;
import com.example.serversideclinet.dto.PendingAppointmentDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("all")
    public  ResponseEntity<List<Employee>> getAllEmployees(){
        return new ResponseEntity<>(employeeService.getAllEmployees(), HttpStatus.OK);
    }
    @PostMapping("create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        try {
            Employee created = employeeService.createEmployee(requestDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("employee", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi tạo nhân viên: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("pending")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> getPendingAppointmentsForEmployee(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Integer lighter = currentUser.getEmployeeId();
            if (lighter == null) {
                throw new IllegalStateException("Không tìm thấy ID nhân viên trong thông tin xác thực");
            }
            List<PendingAppointmentDTO> pendingAppointments = employeeService.getPendingAppointmentsForEmployee(lighter);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appointments", pendingAppointments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi lấy danh sách cuộc hẹn: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("appointments/{appointmentId}/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> updateAppointmentStatus(
            @PathVariable Integer appointmentId,
            @RequestParam("action") String action,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            if (action == null || action.trim().isEmpty()) {
                throw new IllegalArgumentException("Hành động không được để trống");
            }
            Integer lighter = currentUser.getEmployeeId();
            if (lighter == null) {
                throw new IllegalStateException("Không tìm thấy ID nhân viên trong thông tin xác thực");
            }
            AppointmentStatusResponseDTO responseDTO = employeeService.updateAppointmentStatus(lighter, appointmentId, action);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appointment", responseDTO);
            return ResponseEntity.ok(response);
        } catch (MessagingException | IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi gửi email cho hành động '" + action + "': " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi cập nhật trạng thái cuộc hẹn với hành động '" + action + "': " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}