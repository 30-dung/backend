package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.SalaryRules;
import com.example.serversideclinet.service.SalaryRulesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/salary-rules")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class SalaryRulesController {

    @Autowired
    private SalaryRulesService salaryRulesService;

    @Operation(summary = "Get all salary rules")
    @GetMapping
    public ResponseEntity<List<SalaryRules>> getAllSalaryRules() {
        return ResponseEntity.ok(salaryRulesService.findAll());
    }

    @Operation(summary = "Get salary rule by ID")
    @GetMapping("/{id}")
    public ResponseEntity<SalaryRules> getSalaryRuleById(@PathVariable int id) {
        return ResponseEntity.ok(salaryRulesService.findById(id));
    }

    @Operation(summary = "Add a new salary rule")
    @PostMapping
    public ResponseEntity<SalaryRules> addSalaryRule(@RequestBody SalaryRules salaryRules) {
        return ResponseEntity.ok(salaryRulesService.addSalaryRules(salaryRules));
    }

    @Operation(summary = "Update an existing salary rule")
    @PutMapping("/{id}")
    public ResponseEntity<SalaryRules> updateSalaryRule(@PathVariable int id, @RequestBody SalaryRules salaryRules) {
        return ResponseEntity.ok(salaryRulesService.updateSalaryRules(id, salaryRules));
    }

    @Operation(summary = "Delete a salary rule")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalaryRule(@PathVariable int id) {
        salaryRulesService.deleteSalaryRules(id);
        return ResponseEntity.noContent().build();
    }
}