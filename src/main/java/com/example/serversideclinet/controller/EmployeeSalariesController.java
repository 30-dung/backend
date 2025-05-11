package com.example.serversideclinet.controller;

import com.example.serversideclinet.service.EmployeeSalariesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/salaries")
public class EmployeeSalariesController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeSalariesController.class);

    @Autowired
    private EmployeeSalariesService employeeSalariesService;

    @PostMapping("/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> calculateSalaries(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        logger.info("Received request to calculate salaries for year: {}, month: {}", year, month);

        Map<String, Object> response = new HashMap<>();
        try {
            employeeSalariesService.calculateSalariesForMonth(year, month);
            response.put("status", "success");
            response.put("message", "Salary calculation completed for year: " + year + ", month: " + month);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalStateException e) {
            logger.warn("Calculation failed: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            logger.error("Error during salary calculation: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to calculate salaries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}