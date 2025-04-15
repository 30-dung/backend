package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.Review;
import com.example.serversideclinet.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Tạo mới một đánh giá
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review createReview(@RequestBody Review review) {
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
