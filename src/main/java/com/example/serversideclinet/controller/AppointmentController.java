// src/main/java/com/example/serversideclinet/controller/AppointmentController.java
package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.dto.AppointmentResponse;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Appointment.Status;
import com.example.serversideclinet.service.AppointmentService;
import com.example.serversideclinet.service.UserService;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createAppointments(@RequestBody AppointmentRequest[] requests, Authentication authentication) {
        String email = authentication.getName();
        try {
            List<Appointment> createdAppointments = appointmentService.createMultipleAppointments(List.of(requests), email);
            List<AppointmentResponse> responses = createdAppointments.stream().map(this::mapToAppointmentResponse).toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        List<AppointmentResponse> responses = appointments.stream().map(this::mapToAppointmentResponse).toList();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Integer id) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            AppointmentResponse response = mapToAppointmentResponse(appointment);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<AppointmentResponse> getAppointmentBySlug(@PathVariable String slug) {
        try {
            Appointment appointment = appointmentService.getAppointmentBySlug(slug);
            AppointmentResponse response = mapToAppointmentResponse(appointment);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('EMPLOYEE')")
    public ResponseEntity<?> updateAppointment(@PathVariable Integer id, @RequestBody AppointmentRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            Appointment updatedAppointment = appointmentService.updateAppointment(id, request, email);
            AppointmentResponse response = mapToAppointmentResponse(updatedAppointment);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAppointment(@PathVariable Integer id, Authentication authentication) {
        try {
            String email = authentication.getName();
            appointmentService.deleteAppointment(id, email);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByUser(@PathVariable String email) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByUser(email);
            List<AppointmentResponse> responses = appointments.stream().map(this::mapToAppointmentResponse).toList();
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/employee/{email}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByEmployee(
            @PathVariable String email,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        try {
            Appointment.Status appointmentStatus = status != null && !status.equalsIgnoreCase("ALL") ?
                    Appointment.Status.valueOf(status.toUpperCase()) : null;
            List<Appointment> appointments = appointmentService.getAppointmentsByEmployee(email, appointmentStatus, startDate, endDate);
            List<AppointmentResponse> responses = appointments.stream().map(this::mapToAppointmentResponse).toList();
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> confirmAppointment(@PathVariable Integer id) {
        try {
            Appointment appointment = appointmentService.confirmAppointment(id);
            AppointmentResponse response = mapToAppointmentResponse(appointment);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize ("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<?> completeAppointment(@PathVariable Integer id) {
        try {
            Appointment appointment = appointmentService.completeAppoinment(id);
            AppointmentResponse response = mapToAppointmentResponse(appointment);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('EMPLOYEE')")
    public ResponseEntity<?> cancelAppointment(@PathVariable Integer id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Appointment appointment = appointmentService.cancelAppointment(id, email);
            AppointmentResponse response = mapToAppointmentResponse(appointment);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> rejectAppointment(
            @PathVariable Integer id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Appointment appointment = appointmentService.rejectAppointment(id, email, reason != null ? reason : "Không có lý do cụ thể.");
            AppointmentResponse response = mapToAppointmentResponse(appointment);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reassignAppointment(
            @PathVariable Integer id,
            @RequestParam Integer newEmployeeId) {
        try {
            Appointment appointment = appointmentService.reassignAppointment(id, newEmployeeId);
            AppointmentResponse response = mapToAppointmentResponse(appointment);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    private AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setAppointmentId(appointment.getAppointmentId());
        response.setSlug(appointment.getSlug());
        response.setStartTime(appointment.getStartTime().toString());
        response.setEndTime(appointment.getEndTime().toString());
        response.setStatus(appointment.getStatus().toString());
        response.setCreatedAt(appointment.getCreatedAt().toString());

        Integer storeId = null;
        String storeName = "Unknown Store";
        Integer storeServiceId = null;
        String serviceName = "Unknown Service";
        Integer employeeId = null;
        String employeeName = "Unknown Employee";
        String employeeEmail = "Unknown Email";
        Integer userId = null;
        String userName = "Unknown User";
        double totalAmount = 0.0;

        if (appointment.getStoreService() != null) {
            storeServiceId = appointment.getStoreService().getStoreServiceId();
            if (appointment.getStoreService().getStore() != null) {
                storeId = appointment.getStoreService().getStore().getStoreId();
                storeName = appointment.getStoreService().getStore().getStoreName() != null ?
                        appointment.getStoreService().getStore().getStoreName() : "Unknown Store";
            }
            if (appointment.getStoreService().getService() != null) {
                serviceName = appointment.getStoreService().getService().getServiceName() != null ?
                        appointment.getStoreService().getService().getServiceName() : "Unknown Service";
            }
        }

        if (appointment.getEmployee() != null) {
            employeeId = appointment.getEmployee().getEmployeeId();
            employeeName = appointment.getEmployee().getFullName() != null ?
                    appointment.getEmployee().getFullName() : "Unknown Employee";
            employeeEmail = appointment.getEmployee().getEmail() != null ?
                    appointment.getEmployee().getEmail() : "Unknown Email";
        }

        if (appointment.getUser() != null) {
            userId = appointment.getUser().getUserId();
            userName = appointment.getUser().getFullName() != null ?
                    appointment.getUser().getFullName() : "Unknown User";
        }

        if (appointment.getInvoice() != null && appointment.getInvoice().getTotalAmount() != null) {
            totalAmount = appointment.getInvoice().getTotalAmount().doubleValue();
        }

        response.setStoreService(new AppointmentResponse.StoreServiceDetail(storeId, storeServiceId, storeName, serviceName));
        response.setEmployee(new AppointmentResponse.EmployeeDetail(employeeId, employeeName, employeeEmail));
        response.setUser(new AppointmentResponse.UserDetail(userId, userName));
        response.setInvoice(new AppointmentResponse.InvoiceDetailInfo(totalAmount));

        return response;
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> filterAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employeeEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        try {
            Appointment.Status appointmentStatus = status != null && !status.equalsIgnoreCase("ALL") ?
                    Appointment.Status.valueOf(status.toUpperCase()) : null;
            List<Appointment> appointments = appointmentService.filterAppointments(appointmentStatus, employeeEmail, startDate, endDate);
            log.info("Raw appointments from service: {}", appointments);
            List<AppointmentResponse> responses = appointments.stream().map(this::mapToAppointmentResponse).toList();
            log.info("Mapped responses: {}", responses);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}