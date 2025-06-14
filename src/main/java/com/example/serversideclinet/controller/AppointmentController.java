package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.dto.AppointmentResponse;
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
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('EMPLOYEE')")
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
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByEmployee(@PathVariable String email) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByEmployee(email);
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
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('EMPLOYEE')")
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

    private AppointmentResponse mapToAppointmentResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setAppointmentId(appointment.getAppointmentId());
        response.setSlug(appointment.getSlug());
        response.setStartTime(appointment.getStartTime().toString());
        response.setEndTime(appointment.getEndTime().toString());
        response.setStatus(appointment.getStatus().toString());
        response.setCreatedAt(appointment.getCreatedAt().toString()); // Thêm ánh xạ createdAt
        String storeName = "Unknown Store";
        String serviceName = "Unknown Service";
        if (appointment.getStoreService() != null) {
            if (appointment.getStoreService().getStore() != null) {
                storeName = appointment.getStoreService().getStore().getStoreName() != null ?
                        appointment.getStoreService().getStore().getStoreName() : "Unknown Store";
            }
            if (appointment.getStoreService().getService() != null) {
                serviceName = appointment.getStoreService().getService().getServiceName() != null ?
                        appointment.getStoreService().getService().getServiceName() : "Unknown Service";
            }
        }

        response.setStoreService(new AppointmentResponse.StoreService(storeName, serviceName));
        response.setEmployee(new AppointmentResponse.Employee(
                appointment.getEmployee() != null && appointment.getEmployee().getFullName() != null ?
                        appointment.getEmployee().getFullName() : "Unknown Employee"
        ));
        response.setUser(new AppointmentResponse.User(
                appointment.getUser() != null && appointment.getUser().getFullName() != null ?
                        appointment.getUser().getFullName() : "Unknown User"
        ));
        response.setInvoice(new AppointmentResponse.Invoice(
                appointment.getInvoice() != null && appointment.getInvoice().getTotalAmount() != null ?
                        appointment.getInvoice().getTotalAmount().doubleValue() : 0.0
        ));
        return response;
    }
}