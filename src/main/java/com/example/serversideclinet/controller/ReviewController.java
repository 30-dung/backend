package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.MultiReviewRequest;
import com.example.serversideclinet.model.Review;
import com.example.serversideclinet.model.ReviewTargetType;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@PreAuthorize("hasRole('CUSTOMER')") // Áp dụng cho tất cả các API bên dưới
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/multi")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Review> createMultipleReviews(@RequestBody MultiReviewRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = reviewService.findUserByEmail(email);

        List<Review> reviews = new ArrayList<>();

        // Review for store
        if (request.getStoreId() != null && request.getStoreRating() != null) {
            Review storeReview = new Review();
            storeReview.setUser(user);
            storeReview.setTargetType(ReviewTargetType.STORE);
            storeReview.setTargetId(request.getStoreId());
            storeReview.setRating(request.getStoreRating());
            storeReview.setComment(request.getComment());
            reviews.add(reviewService.createReview(storeReview));
        }

        // Review for service
        if (request.getServiceId() != null && request.getServiceRating() != null) {
            Review serviceReview = new Review();
            serviceReview.setUser(user);
            serviceReview.setTargetType(ReviewTargetType.SERVICE);
            serviceReview.setTargetId(request.getServiceId());
            serviceReview.setRating(request.getServiceRating());
            serviceReview.setComment(request.getComment());
            reviews.add(reviewService.createReview(serviceReview));
        }

        // Review for employee
        if (request.getEmployeeId() != null && request.getEmployeeRating() != null) {
            Review employeeReview = new Review();
            employeeReview.setUser(user);
            employeeReview.setTargetType(ReviewTargetType.EMPLOYEE);
            employeeReview.setTargetId(request.getEmployeeId());
            employeeReview.setRating(request.getEmployeeRating());
            employeeReview.setComment(request.getComment());
            reviews.add(reviewService.createReview(employeeReview));
        }

        return reviews;
    }

}
