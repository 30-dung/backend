package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Review;
import com.example.serversideclinet.model.ReviewTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    boolean existsByAppointmentAppointmentId(Integer appointmentId);
    // Phương thức hiện có: Tìm review cụ thể theo Appointment, Target và TargetType
    Optional<Review> findByAppointmentAppointmentIdAndTargetIdAndTargetType(Integer appointmentId, Integer targetId, ReviewTargetType targetType);

    // THÊM CÁC PHƯƠNG THỨC NÀY VÀO ĐÂY
    // Lấy tất cả reviews cho một targetId và targetType cụ thể (dùng để tính averageRating on-demand)
    List<Review> findAllByTargetIdAndTargetType(Integer targetId, ReviewTargetType targetType);

    // Phương thức để lấy tất cả reviews có targetType là STORE cho một StoreId cụ thể
    // dùng cho việc tính tổng review và rating của riêng STORE
    @Query("SELECT r FROM Review r LEFT JOIN r.appointment a LEFT JOIN a.storeService ss WHERE " +
            "(r.targetType = 'STORE' AND r.targetId = :storeId)")
    List<Review> findAllByStoreIdAndTargetType(@Param("storeId") Integer storeId, @Param("targetType") ReviewTargetType targetType);
    // KẾT THÚC THÊM CÁC PHƯƠNG THỨC NÀY

    @Query("SELECT r FROM Review r " +
            "LEFT JOIN r.appointment a " +
            "LEFT JOIN a.employee e " +
            "LEFT JOIN a.storeService ss " +
            "WHERE (r.targetType = 'STORE' AND r.targetId = :storeId) " +
            "OR (r.targetType = 'EMPLOYEE' AND e.store.storeId = :storeId AND r.targetId = e.employeeId) " +
            "OR (r.targetType = 'STORE_SERVICE' AND ss.store.storeId = :storeId AND r.targetId = ss.storeServiceId)")
    Page<Review> findReviewsByStoreId(@Param("storeId") Integer storeId, Pageable pageable);
    @Query("SELECT r FROM Review r JOIN r.appointment a JOIN a.employee e " +
            "WHERE e.store.storeId = :storeId AND r.targetId = :employeeId AND r.targetType = 'EMPLOYEE'")
    Page<Review> findReviewsByStoreIdAndEmployeeId(@Param("storeId") Integer storeId, @Param("employeeId") Integer employeeId, Pageable pageable);
    @Query("SELECT r FROM Review r JOIN r.appointment a JOIN a.storeService ss " +
            "WHERE ss.store.storeId = :storeId AND r.targetId = :storeServiceId AND r.targetType = 'STORE_SERVICE'")
    Page<Review> findReviewsByStoreIdAndStoreServiceId(@Param("storeId") Integer storeId, @Param("storeServiceId") Integer storeServiceId, Pageable pageable);
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN r.appointment a " +
            "LEFT JOIN a.employee e " +
            "LEFT JOIN a.storeService ss " +
            "WHERE ( (r.targetType = 'STORE' AND r.targetId = :storeId) " +
            "OR (r.targetType = 'EMPLOYEE' AND e.store.storeId = :storeId AND " +
            "r.targetId = e.employeeId) " +
            "OR (r.targetType = 'STORE_SERVICE' AND ss.store.storeId = :storeId AND r.targetId = ss.storeServiceId) ) " +
            "AND r.rating = :rating")
    Page<Review> findReviewsByStoreIdAndRating(@Param("storeId") Integer storeId, @Param("rating") Integer rating, Pageable pageable);
    @Query("SELECT r FROM Review r JOIN r.appointment a JOIN a.employee e " +
            "WHERE e.store.storeId = :storeId AND r.targetId = :employeeId AND r.targetType = 'EMPLOYEE' AND r.rating = :rating")
    Page<Review> findReviewsByStoreIdAndEmployeeIdAndRating(@Param("storeId") Integer storeId, @Param("employeeId") Integer employeeId, @Param("rating") Integer rating, Pageable pageable);
    @Query("SELECT r FROM Review r JOIN r.appointment a JOIN a.storeService ss " +
            "WHERE ss.store.storeId = :storeId AND r.targetId = :storeServiceId AND r.targetType = 'STORE_SERVICE' AND r.rating = :rating")
    Page<Review> findReviewsByStoreIdAndStoreServiceIdAndRating(@Param("storeId") Integer storeId, @Param("storeServiceId") Integer storeServiceId, @Param("rating") Integer rating, Pageable pageable);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetId = :targetId AND r.targetType = :targetType")
    Optional<Double> calculateAverageRatingForTarget(@Param("targetId") Integer targetId, @Param("targetType") ReviewTargetType targetType);
    @Query("SELECT COUNT(r) FROM Review r WHERE r.targetId = :targetId AND r.targetType = :targetType")
    Long countReviewsForTarget(@Param("targetId") Integer targetId, @Param("targetType") ReviewTargetType targetType);
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.targetId = :targetId AND r.targetType = :targetType GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionForTarget(@Param("targetId") Integer targetId, @Param("targetType") ReviewTargetType targetType);
    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
            "LEFT JOIN r.appointment a " +
            "LEFT JOIN a.employee e " +
            "LEFT JOIN a.storeService ss " +
            "WHERE ( (r.targetType = 'STORE' AND r.targetId = :storeId) " +
            "OR (r.targetType = 'EMPLOYEE' AND e.store.storeId = :storeId " +
            "AND r.targetId = e.employeeId) " +
            "OR (r.targetType = 'STORE_SERVICE' AND ss.store.storeId = :storeId AND r.targetId = ss.storeServiceId) ) " +
            "GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionForStore(@Param("storeId") Integer storeId);
    @Query("SELECT DISTINCT r.targetId FROM Review r JOIN r.appointment a JOIN a.employee e " +
            "WHERE r.targetType = 'EMPLOYEE' AND e.store.storeId = :storeId")
    List<Integer> findReviewedEmployeeIdsByStoreId(@Param("storeId") Integer storeId);
    @Query("SELECT DISTINCT r.targetId FROM Review r JOIN r.appointment a JOIN a.storeService ss " +
            "WHERE r.targetType = 'STORE_SERVICE' AND ss.store.storeId = :storeId")
    List<Integer> findReviewedStoreServiceIdsByStoreId(@Param("storeId") Integer storeId);
}