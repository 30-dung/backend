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
@RequestMapping("/employee/working-time-slots")
public class WorkingTimeSlotController {
    @Autowired
    private WorkingTimeSlotService slotService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<WorkingTimeSlot> create(@RequestBody WorkingTimeSlotRequest request){
        WorkingTimeSlot createdSot = slotService.createSlot(request);
        return ResponseEntity.ok(createdSot);
    }
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<WorkingTimeSlot>> getMyWorkingSlots() {
        List<WorkingTimeSlot> slots = slotService.getMyWorkingSlots();
        return ResponseEntity.ok(slots);
    }

}
