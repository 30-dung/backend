package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.dto.RatingRequest;
import com.example.serversideclinet.service.CustomerSatisfactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequestMapping("/api/customer-satisfaction")
public class CustomerSatisfactionController {

    @Autowired
    private CustomerSatisfactionService customerSatisfactionService;

    /**
     * Endpoint to submit customer satisfaction rating
     * Uses path variable for appointment ID instead of request body
     */
    @PostMapping("/{appointmentId}")
    public ResponseEntity<CustomerSatisfaction> submitRating(
            @PathVariable Integer appointmentId,
            @RequestBody RatingRequest ratingRequest) {

        // Call service to process the rating submission
        CustomerSatisfaction savedSatisfaction = customerSatisfactionService.processRating(
                appointmentId,
                ratingRequest.getRating(),
                ratingRequest.getFeedback()
        );

        // Return the saved satisfaction data
        return ResponseEntity.ok(savedSatisfaction);
    }
}