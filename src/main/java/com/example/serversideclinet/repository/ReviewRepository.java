package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Review;
import com.example.serversideclinet.model.ReviewTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.user.userId = :userId AND r.targetType = :targetType AND r.targetId = :targetId")
    boolean existsByUserIdAndTargetTypeAndTargetId(Integer userId, ReviewTargetType targetType, Integer targetId);

}
