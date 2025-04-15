package com.example.serversideclinet.service;

import com.example.serversideclinet.model.Review;
import com.example.serversideclinet.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    // thêm đánh giá
    public Review createReview(Review review) {
        // Kiểm tra các điều kiện đầu vào nếu cần (ví dụ: rating phải trong phạm vi từ 1 đến 5)
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return reviewRepository.save(review); // Lưu review mới
    }
    public Review getReviewById(int id) {
        return reviewRepository.findById(id).orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }
    public Review updateReview(Integer id, Review updatedReview) {
        return reviewRepository.findById(id).map(review -> {
            // Cập nhật thông tin review nếu nó tồn tại
            review.setRating(updatedReview.getRating());
            review.setComment(updatedReview.getComment());
            review.setTargetType(updatedReview.getTargetType());
            review.setTargetId(updatedReview.getTargetId());
            // Cập nhật thêm các trường khác nếu cần
            return reviewRepository.save(review); // Lưu review đã cập nhật
        }).orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }
    public void deleteReview(int id) {
        reviewRepository.deleteById(id);
    }

}
