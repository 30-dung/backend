package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.CustomerRatingDTO;
import com.example.serversideclinet.service.CustomerRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rating")
public class CustomerRatingController {

    @Autowired
    private CustomerRatingService customerRatingService;

    @PostMapping("/rate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerRatingDTO rateEmployee(@RequestParam Integer appointmentId,
                                          @RequestParam Integer stars,
                                          @RequestParam(required = false) String comment) {
        return customerRatingService.rateEmployee(appointmentId, stars, comment);
    }
}
