package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // Create Appointment
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentRequest request, Authentication authentication) {
        String email = authentication.getName();
        try {
            Appointment created = appointmentService.createAppointment(request, email);  // Return Appointment entity directly
            return ResponseEntity.status(HttpStatus.CREATED).body(created);  // Return Appointment entity directly
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid appointment request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("System error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        // Directly return the list of Appointment entities
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Appointment>> getAllPendingAppointments() {
        // Directly return the list of Appointment entities
        List<Appointment> appointments = appointmentService.getAllPendingAppointments();
        return ResponseEntity.ok(appointments);
    }

    // Error Handling for Validation
    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    public ResponseEntity<String> handleValidationException(javax.validation.ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Validation error: " + ex.getMessage());
    }
}
