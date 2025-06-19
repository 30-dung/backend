// src/main/java/com/example/serversideclinet/repository/ReviewReplyRepository.java
package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Integer> {
    // Phương thức này để lấy tất cả replies gốc cho một review (parent_reply_id is NULL)
    // JPA sẽ tự động load children replies nếu childrenReplies được cấu hình EAGER hoặc trong transaction.
    List<ReviewReply> findByReviewReviewIdAndParentReplyIsNullOrderByCreatedAtAsc(Integer reviewId);
}