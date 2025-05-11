package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.service.AppointmentService;
import com.example.serversideclinet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request, Authentication authentication) {
        String email = authentication.getName();
        try {
            Appointment created = appointmentService.createAppointment(request, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }


    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }


}
