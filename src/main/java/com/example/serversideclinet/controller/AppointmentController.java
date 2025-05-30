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
@CrossOrigin(origins = "http://localhost:5173")
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
            List<Appointment> createdAppointments = appointmentService.createMultipleAppointments(requests, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointments);
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

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Integer id) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            AppointmentResponse response = new AppointmentResponse();
            response.setAppointmentId(appointment.getAppointmentId());
            response.setStartTime(appointment.getStartTime().toString());
            response.setEndTime(appointment.getEndTime().toString());
            response.setStatus(appointment.getStatus().toString());
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
            response.setInvoice(new AppointmentResponse.Invoice(
                    appointment.getInvoice() != null && appointment.getInvoice().getTotalAmount() != null ?
                            appointment.getInvoice().getTotalAmount().doubleValue() : 0.0
            ));
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
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
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
    public ResponseEntity<List<Appointment>> getAppointmentsByUser(@PathVariable String email) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByUser(email);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/employee/{email}")
    public ResponseEntity<List<Appointment>> getAppointmentsByEmployee(@PathVariable String email) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByEmployee(email);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> confirmAppointment(@PathVariable Integer id) {
        try {
            Appointment appointment = appointmentService.confirmAppointment(id);
            return new ResponseEntity<>(appointment, HttpStatus.OK);
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
            return new ResponseEntity<>(appointment, HttpStatus.OK);
        } catch (AppointmentService.AppointmentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}