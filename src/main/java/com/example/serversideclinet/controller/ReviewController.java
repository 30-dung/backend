package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.Review;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('CUSTOMER')") // Áp dụng cho tất cả các API bên dưới
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review createReview(@RequestBody Review review) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Lấy username/email từ token
        // Tìm User từ email
        User user = reviewService.findUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        review.setUser(user); // Gán user vào review
        return reviewService.createReview(review);
    }

    // Cập nhật một đánh giá
    @PutMapping("/{id}")
    public Review updateReview(@PathVariable Integer id, @RequestBody Review updatedReview) {
        return reviewService.updateReview(id, updatedReview);
    }

    // Xóa một đánh giá
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable int id) {
        reviewService.deleteReview(id);
    }

    // Lấy thông tin một đánh giá
    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable int id) {
        return reviewService.getReviewById(id);
    }
}
