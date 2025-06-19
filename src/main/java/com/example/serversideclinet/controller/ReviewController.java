// src/main/java/com/example/serversideclinet/controller/ReviewController.java
package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.*;
import com.example.serversideclinet.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // API: Gửi đánh giá mới (dùng sau khi lịch hẹn COMPLETED)
    // POST /api/reviews
    @PostMapping
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewRequestDTO reviewRequest) {
        try {
            ReviewResponseDTO createdReview = reviewService.createReview(reviewRequest);
            return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error during review creation: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{reviewId}/replies")
    public ResponseEntity<?> addReplyToReview(@PathVariable Integer reviewId,
                                              @Valid @RequestBody ReviewReplyRequestDTO replyRequest) {
        replyRequest.setReviewId(reviewId);
        try {
            ReviewReplyResponseDTO createdReply = reviewService.addReplyToReview(replyRequest);
            return new ResponseEntity<>(createdReply, HttpStatus.CREATED);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // THAY ĐỔI QUAN TRỌNG: Kiểu trả về là Page<CombinedReviewDisplayDTO>
    @GetMapping("/store/{storeId}/filtered")
    public ResponseEntity<Page<CombinedReviewDisplayDTO>> getReviewsByStoreIdFiltered(
            @PathVariable Integer storeId,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer storeServiceId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CombinedReviewDisplayDTO> reviews = reviewService.getReviewsByStoreIdFiltered(storeId, employeeId, storeServiceId, rating, pageable);
        return ResponseEntity.ok(reviews);
    }

    // API: Lấy tổng quan đánh giá của một cửa hàng (average rating, phân phối sao, top nhân viên/dịch vụ)
    // GET /api/reviews/store/{storeId}/summary
    @GetMapping("/store/{storeId}/summary")
    public ResponseEntity<?> getOverallStoreRatings(@PathVariable Integer storeId) {
        try {
            OverallRatingDTO summary = reviewService.getOverallStoreRatings(storeId);
            return ResponseEntity.ok(summary);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // API: Kiểm tra xem một lịch hẹn đã được đánh giá chưa
    // GET /api/reviews/existsByAppointmentId?appointmentId={appointmentId}
    @GetMapping("/existsByAppointmentId")
    public ResponseEntity<Boolean> checkIfAppointmentReviewed(@RequestParam Integer appointmentId) {
        boolean exists = reviewService.checkIfAppointmentReviewed(appointmentId);
        return ResponseEntity.ok(exists);
    }
}