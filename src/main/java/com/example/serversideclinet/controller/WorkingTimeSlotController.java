package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.WorkingTimeSlotRequest;
import com.example.serversideclinet.model.WorkingTimeSlot;
import com.example.serversideclinet.service.WorkingTimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequestMapping("/api/working-time-slots")
public class WorkingTimeSlotController {
    @Autowired
    private WorkingTimeSlotService slotService;

    @GetMapping("/available")
    public ResponseEntity<List<WorkingTimeSlot>> getAvailableSlots(
            @RequestParam Integer employeeId,
            @RequestParam String date) {
        List<WorkingTimeSlot> slots = slotService.getAvailableSlots(employeeId, date);
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/registration")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<WorkingTimeSlot> create(@RequestBody WorkingTimeSlotRequest request){
        WorkingTimeSlot createdSot = slotService.createSlot(request);
        return ResponseEntity.ok(createdSot);
    }
    @GetMapping("/list")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<WorkingTimeSlot>> getMyWorkingSlots() {
        List<WorkingTimeSlot> slots = slotService.getMyWorkingSlots();
        return ResponseEntity.ok(slots);
    }

}