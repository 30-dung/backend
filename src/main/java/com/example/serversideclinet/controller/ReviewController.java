package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.MultiReviewRequestDTO;
import com.example.serversideclinet.model.Review;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@PreAuthorize("hasRole('CUSTOMER')")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/multi-rating")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Review> createMultiReviews(@RequestBody MultiReviewRequestDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = reviewService.findUserByEmail(email);

        return reviewService.createMultiReviewsFromInvoice(request, user);
    }


}
