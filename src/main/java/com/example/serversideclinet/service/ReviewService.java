package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.*;
import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewReplyRepository reviewReplyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private StoreServiceRepository storeServiceRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO reviewRequest) {
        logger.info("Received review request for appointment ID: {}, target ID: {}, target type: {}",
                reviewRequest.getAppointmentId(), reviewRequest.getTargetId(), reviewRequest.getTargetType());
        Appointment appointment = appointmentRepository.findById(reviewRequest.getAppointmentId())
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with ID: " + reviewRequest.getAppointmentId()));
        if (appointment.getStatus() != Appointment.Status.COMPLETED) {
            throw new IllegalStateException("Only completed appointments can be reviewed.");
        }

        User user = userRepository.findById(reviewRequest.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + reviewRequest.getUserId()));
        Review review;
        Optional<Review> existingReviewOpt = reviewRepository.findByAppointmentAppointmentIdAndTargetIdAndTargetType(
                reviewRequest.getAppointmentId(),
                reviewRequest.getTargetId(),
                reviewRequest.getTargetType()
        );
        if (existingReviewOpt.isPresent()) {
            review = existingReviewOpt.get();
            review.setRating(reviewRequest.getRating());
            review.setComment(reviewRequest.getComment());
            logger.info("Updating existing review (ID: {}) for appointment ID: {}, target type: {}", review.getReviewId(), reviewRequest.getAppointmentId(), reviewRequest.getTargetType());
        } else {
            review = new Review();
            review.setUser(user);
            review.setAppointment(appointment);
            review.setTargetId(reviewRequest.getTargetId());
            review.setTargetType(reviewRequest.getTargetType());
            review.setRating(reviewRequest.getRating());
            review.setComment(reviewRequest.getComment());
            review.setCreatedAt(LocalDateTime.now());
            logger.info("Creating new review for appointment ID: {}, target type: {}", reviewRequest.getAppointmentId(), reviewRequest.getTargetType());
        }

        Review savedReview = reviewRepository.save(review);
        logger.info("Review saved/updated to DB with ID: {}", savedReview.getReviewId());
        try {
            if (appointment.getStoreService() != null && appointment.getStoreService().getStore() != null) {
                Integer storeId = appointment.getStoreService().getStore().getStoreId();
                logger.debug("Attempting to update average rating for store ID: {}", storeId);
                updateStoreAverageRating(storeId);
                logger.debug("Successfully updated average rating for store ID: {}", storeId);
            } else {
                logger.warn("Could not find store service or store for appointment ID: {}. Skipping store average rating update.", appointment.getAppointmentId());
            }

            // THÊM CÁC DÒNG NÀY ĐỂ CẬP NHẬT RATING CHO EMPLOYEE VÀ STORE_SERVICE VÀO ĐÂY
            // Cập nhật rating cho Employee
            if (reviewRequest.getTargetType() == ReviewTargetType.EMPLOYEE && reviewRequest.getTargetId() != null) {
                logger.debug("Attempting to update average rating for employee ID: {}", reviewRequest.getTargetId());
                updateEmployeeAverageRating(reviewRequest.getTargetId());
                logger.debug("Successfully updated average rating for employee ID: {}", reviewRequest.getTargetId());
            }

            // Cập nhật rating cho StoreService
            if (reviewRequest.getTargetType() == ReviewTargetType.STORE_SERVICE && reviewRequest.getTargetId() != null) {
                logger.debug("Attempting to update average rating for store service ID: {}", reviewRequest.getTargetId());
                updateStoreServiceAverageRating(reviewRequest.getTargetId());
                logger.debug("Successfully updated average rating for store service ID: {}", reviewRequest.getTargetId());
            }
            // KẾT THÚC THÊM CÁC DÒNG NÀY

        } catch (Exception e) {
            logger.error("Error during average rating update for appointment ID {}: {}", appointment.getAppointmentId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update average rating: " + e.getMessage(), e);
        }

        return convertToReviewResponseDTO(savedReview);
    }

    @Transactional
    public ReviewReplyResponseDTO addReplyToReview(ReviewReplyRequestDTO replyRequest) {
        Review review = reviewRepository.findById(replyRequest.getReviewId())
                .orElseThrow(() -> new NoSuchElementException("Review not found with ID: " + replyRequest.getReviewId()));
        User user = userRepository.findById(replyRequest.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + replyRequest.getUserId()));
        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setUser(user);
        reply.setComment(replyRequest.getComment());
        reply.setIsStoreReply(replyRequest.getIsStoreReply());

        if (replyRequest.getParentReplyId() != null) {
            ReviewReply parentReply = reviewReplyRepository.findById(replyRequest.getParentReplyId())
                    .orElseThrow(() -> new NoSuchElementException("Parent reply not found with ID: " + replyRequest.getParentReplyId()));
            reply.setParentReply(parentReply);
        }

        ReviewReply savedReply = reviewReplyRepository.save(reply);
        return convertToReviewReplyResponseDTO(savedReply);
    }

    @Transactional
    public Page<CombinedReviewDisplayDTO> getReviewsByStoreIdFiltered(
            Integer storeId,
            Integer employeeId,
            Integer storeServiceId,
            Integer rating,
            Pageable pageable) {

        Page<Review> filteredReviewsPage;
        if (employeeId != null && rating != null) {
            filteredReviewsPage = reviewRepository.findReviewsByStoreIdAndEmployeeIdAndRating(storeId, employeeId, rating, pageable);
        } else if (employeeId != null) {
            filteredReviewsPage = reviewRepository.findReviewsByStoreIdAndEmployeeId(storeId, employeeId, pageable);
        } else if (storeServiceId != null && rating != null) {
            filteredReviewsPage = reviewRepository.findReviewsByStoreIdAndStoreServiceIdAndRating(storeId, storeServiceId, rating, pageable);
        } else if (storeServiceId != null) {
            filteredReviewsPage = reviewRepository.findReviewsByStoreIdAndStoreServiceId(storeId, storeServiceId, pageable);
        } else if (rating != null) {
            filteredReviewsPage = reviewRepository.findReviewsByStoreIdAndRating(storeId, rating, pageable);
        } else {
            filteredReviewsPage = reviewRepository.findReviewsByStoreId(storeId, pageable);
        }

        List<Review> reviewsInCurrentPage = filteredReviewsPage.getContent();

        Map<String, CombinedReviewDisplayDTO> combinedReviewsMap = new HashMap<>();
        for (Review review : reviewsInCurrentPage) {
            String key = review.getAppointment().getAppointmentId() + "_" + review.getUser().getUserId();
            combinedReviewsMap.computeIfAbsent(key, k -> {
                UserInfoDTO reviewerInfo = new UserInfoDTO();
                reviewerInfo.setUserId(review.getUser().getUserId());
                reviewerInfo.setFullName(review.getUser().getFullName());
                reviewerInfo.setEmail(review.getUser().getEmail());

                Store apptStore = (review.getAppointment().getStoreService() != null) ? review.getAppointment().getStoreService().getStore() : null;

                Employee apptEmployee = review.getAppointment().getEmployee();
                ServiceEntity apptService = (review.getAppointment().getStoreService() != null) ? review.getAppointment().getStoreService().getService() : null;

                Integer mainReviewId = null;
                if (review.getTargetType() == ReviewTargetType.STORE) {
                    mainReviewId = review.getReviewId();
                } else {
                    mainReviewId = review.getReviewId();
                }

                return new CombinedReviewDisplayDTO(
                        reviewerInfo,
                        review.getAppointment().getAppointmentId(),
                        review.getAppointment().getSlug(),
                        apptStore != null ? apptStore.getStoreName() : "Unknown Store",
                        apptStore != null ? apptStore.getStoreId() : null,
                        apptEmployee != null ? apptEmployee.getFullName() : "Unknown Employee",
                        apptEmployee != null ? apptEmployee.getEmployeeId() : null,
                        apptService != null ? apptService.getServiceName() : "Unknown Service",
                        (review.getAppointment().getStoreService() != null) ? review.getAppointment().getStoreService().getStoreServiceId() : null,
                        review.getComment(),
                        review.getCreatedAt(),
                        new ArrayList<>(),
                        mainReviewId
                );
            });
            CombinedReviewDisplayDTO combinedDTO = combinedReviewsMap.get(key);

            if (review.getTargetType() == ReviewTargetType.STORE && (combinedDTO.getMainReviewId() == null ||
                    (reviewRepository.findById(combinedDTO.getMainReviewId()).isPresent() && reviewRepository.findById(combinedDTO.getMainReviewId()).get().getTargetType() != ReviewTargetType.STORE))) {
                combinedDTO.setMainReviewId(review.getReviewId());
            }

            if (review.getTargetType() == ReviewTargetType.STORE) {
                combinedDTO.setStoreRating(review.getRating());
                if (review.getComment() != null && !review.getComment().isEmpty()) {
                    combinedDTO.setComment(review.getComment());
                }
                if (review.getCreatedAt() != null && (combinedDTO.getCreatedAt() == null || review.getCreatedAt().isAfter(combinedDTO.getCreatedAt()))) {
                    combinedDTO.setCreatedAt(review.getCreatedAt());
                }
            } else if (review.getTargetType() == ReviewTargetType.EMPLOYEE) {
                combinedDTO.setEmployeeRating(review.getRating());
            } else if (review.getTargetType() == ReviewTargetType.STORE_SERVICE) {
                combinedDTO.setServiceRating(review.getRating());
            }

            if(review.getReviewId() != null) {
                List<ReviewReply> rootRepliesForThisReview = reviewReplyRepository.findByReviewReviewIdAndParentReplyIsNullOrderByCreatedAtAsc(review.getReviewId());
                List<ReviewReplyResponseDTO> tree = buildReplyTree(rootRepliesForThisReview);
                combinedDTO.getReplies().addAll(tree);
            }
        }

        List<CombinedReviewDisplayDTO> combinedReviewsList = new ArrayList<>(combinedReviewsMap.values());
        combinedReviewsList.sort(Comparator.comparing(CombinedReviewDisplayDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        return new PageImpl<>(combinedReviewsList, pageable, filteredReviewsPage.getTotalElements());
    }

    public OverallRatingDTO getOverallStoreRatings(Integer storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NoSuchElementException("Store not found with ID: " + storeId));
        OverallRatingDTO overallRatingDTO = new OverallRatingDTO();
        overallRatingDTO.setStoreId(store.getStoreId());
        overallRatingDTO.setStoreName(store.getStoreName());
        overallRatingDTO.setStoreImageUrl(store.getStoreImages()); // GÁN HÌNH ẢNH STORE

        // SỬA ĐOẠN NÀY: Lấy tổng số reviews và rating trung bình từ Store entity
        overallRatingDTO.setTotalReviews(store.getTotalReviews());
        overallRatingDTO.setAverageRating(store.getAverageRating());
        // KẾT THÚC SỬA

        // Lấy phân phối sao từ reviews của Store (phần này giữ nguyên)
        Map<Integer, Long> ratingDistribution = reviewRepository.getRatingDistributionForStore(storeId).stream()
                .collect(Collectors.toMap(
                        arr -> (Integer) arr[0],
                        arr -> (Long) arr[1]
                ));
        overallRatingDTO.setRatingDistribution(ratingDistribution);

        // SỬA ĐOẠN NÀY: Lấy thông tin nhân viên và rating của họ từ Employee entity
        List<EmployeeRatingSummaryDTO> employeeSummaries = employeeRepository.findByStoreStoreId(storeId).stream() // Lấy tất cả nhân viên của cửa hàng
                .filter(employee -> employee.getTotalReviews() > 0) // Chỉ những nhân viên có đánh giá
                .map(employee -> {
                    EmployeeRatingSummaryDTO dto = new EmployeeRatingSummaryDTO();
                    dto.setEmployeeId(employee.getEmployeeId());
                    dto.setEmployeeName(employee.getFullName());
                    dto.setAverageRating(employee.getAverageRating()); // Lấy từ Employee entity
                    dto.setTotalReviews(employee.getTotalReviews()); // Lấy từ Employee entity
                    dto.setAvatarUrl(employee.getAvatarUrl());
                    return dto;
                })
                .collect(Collectors.toList());
        overallRatingDTO.setEmployeeRatings(employeeSummaries);
        // KẾT THÚC SỬA

        // SỬA ĐOẠN NÀY: Lấy thông tin dịch vụ và rating của họ từ StoreService entity
        List<ServiceRatingSummaryDTO> serviceSummaries = storeServiceRepository.findByStoreStoreId(storeId).stream() // Lấy tất cả dịch vụ của cửa hàng
                .filter(storeService -> storeService.getTotalReviews() > 0) // Chỉ những dịch vụ có đánh giá
                .map(storeService -> {
                    ServiceEntity serviceEntity = storeService.getService();
                    ServiceRatingSummaryDTO dto = new ServiceRatingSummaryDTO();
                    dto.setServiceId(serviceEntity.getServiceId());
                    dto.setServiceName(serviceEntity.getServiceName());
                    dto.setAverageRating(storeService.getAverageRating()); // Lấy từ StoreService entity
                    dto.setTotalReviews(storeService.getTotalReviews()); // Lấy từ StoreService entity
                    dto.setServiceImg(serviceEntity.getServiceImg());
                    return dto;
                })
                .collect(Collectors.toList());
        overallRatingDTO.setServiceRatings(serviceSummaries);
        // KẾT THÚC SỬA

        return overallRatingDTO;
    }

    public ReviewResponseDTO getReviewById(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review not found with ID: " + reviewId));
        return convertToReviewResponseDTO(review);
    }

    public boolean checkIfAppointmentReviewed(Integer appointmentId) {
        return reviewRepository.existsByAppointmentAppointmentId(appointmentId);
    }

    private ReviewResponseDTO convertToReviewResponseDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setReviewId(review.getReviewId());

        UserInfoDTO reviewerInfo = new UserInfoDTO();
        reviewerInfo.setUserId(review.getUser().getUserId());
        reviewerInfo.setFullName(review.getUser().getFullName());
        reviewerInfo.setEmail(review.getUser().getEmail());
        dto.setReviewer(reviewerInfo);

        dto.setAppointmentId(review.getAppointment().getAppointmentId());
        dto.setTargetId(review.getTargetId());
        dto.setTargetType(review.getTargetType());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        Store store = (review.getAppointment() != null && review.getAppointment().getStoreService() != null) ?
                review.getAppointment().getStoreService().getStore() : null;
        if (store != null) {
            dto.setStoreName(store.getStoreName());
            dto.setStoreId(store.getStoreId());
        }

        switch (review.getTargetType()) {
            case STORE:
                if (store != null) {
                    dto.setTargetName(store.getStoreName());
                } else {
                    dto.setTargetName("Unknown Store");
                }
                break;
            case EMPLOYEE:
                Employee employee = (review.getAppointment() != null) ?
                        review.getAppointment().getEmployee() : null;
                if (employee != null) {
                    dto.setTargetName(employee.getFullName());
                    dto.setEmployeeName(employee.getFullName());
                } else {
                    dto.setTargetName("Unknown Employee");
                }
                break;
            case STORE_SERVICE:
                com.example.serversideclinet.model.StoreService storeService =
                        (review.getAppointment() != null) ?
                                review.getAppointment().getStoreService() : null;
                if (storeService != null && storeService.getService() != null) {
                    dto.setTargetName(storeService.getService().getServiceName());
                    dto.setServiceName(storeService.getService().getServiceName());
                } else {
                    dto.setTargetName("Unknown Service");
                }
                break;
            case SERVICE:
                ServiceEntity serviceEntity = serviceRepository.findById(review.getTargetId()).orElse(null);
                if (serviceEntity != null) {
                    dto.setTargetName(serviceEntity.getServiceName());
                    dto.setServiceName(serviceEntity.getServiceName());
                } else {
                    dto.setTargetName("Unknown Service (generic)");
                }
                break;
        }

        if (review.getReplies() != null && !review.getReplies().isEmpty()) {
            dto.setReplies(review.getReplies().stream()
                    .map(this::convertToReviewReplyResponseDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setReplies(Collections.emptyList());
        }

        return dto;
    }

    private ReviewReplyResponseDTO convertToReviewReplyResponseDTO(ReviewReply reply) {
        ReviewReplyResponseDTO dto = new ReviewReplyResponseDTO();
        dto.setReplyId(reply.getReplyId());
        dto.setReviewId(reply.getReview().getReviewId());

        UserInfoDTO replierInfo = new UserInfoDTO();
        replierInfo.setUserId(reply.getUser().getUserId());
        replierInfo.setFullName(reply.getUser().getFullName());
        replierInfo.setEmail(reply.getUser().getEmail());
        dto.setReplier(replierInfo);

        dto.setComment(reply.getComment());
        dto.setCreatedAt(reply.getCreatedAt());
        dto.setIsStoreReply(reply.getIsStoreReply());
        dto.setParentReplyId(reply.getParentReply() != null ? reply.getParentReply().getReplyId() : null);

        if (reply.getChildrenReplies() != null && !reply.getChildrenReplies().isEmpty()) {
            dto.setChildrenReplies(reply.getChildrenReplies().stream()
                    .map(this::convertToReviewReplyResponseDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setChildrenReplies(Collections.emptyList());
        }

        return dto;
    }

    private List<ReviewReplyResponseDTO> buildReplyTree(List<ReviewReply> rootReplies) {
        Map<Integer, ReviewReplyResponseDTO> replyMap = rootReplies.stream()
                .map(this::convertToReviewReplyResponseDTO)
                .collect(Collectors.toMap(ReviewReplyResponseDTO::getReplyId, dto -> dto));
        List<ReviewReplyResponseDTO> result = new ArrayList<>();

        for (ReviewReplyResponseDTO replyDTO : replyMap.values()) {
            if (replyDTO.getParentReplyId() == null || !replyMap.containsKey(replyDTO.getParentReplyId())) {
                result.add(replyDTO);
            } else {
                ReviewReplyResponseDTO parent = replyMap.get(replyDTO.getParentReplyId());
                if (parent != null) {
                    if (parent.getChildrenReplies() == null) {
                        parent.setChildrenReplies(new ArrayList<>());
                    }
                    parent.getChildrenReplies().add(replyDTO);
                }
            }
        }
        result.sort(Comparator.comparing(ReviewReplyResponseDTO::getCreatedAt));
        return result;
    }

    private void updateStoreAverageRating(Integer storeId) {
        storeRepository.findById(storeId).ifPresent(store -> {
            List<Review> allReviewsForStore = reviewRepository.findAllByStoreIdAndTargetType(storeId, ReviewTargetType.STORE);
            long totalReviews = allReviewsForStore.size();
            double averageRating = totalReviews > 0 ?
                    allReviewsForStore.stream()
                            .mapToDouble(Review::getRating)
                            .average()
                            .orElse(0.0) : 0.0;
            store.setAverageRating(BigDecimal.valueOf(averageRating));
            store.setTotalReviews(totalReviews); // Cập nhật totalReviews vào Store entity
            storeRepository.save(store);
        });
    }

    // THÊM CÁC PHƯƠNG THỨC NÀY VÀO ĐÂY
    private void updateEmployeeAverageRating(Integer employeeId) {
        employeeRepository.findById(employeeId).ifPresent(employee -> {
            List<Review> allReviewsForEmployee = reviewRepository.findAllByTargetIdAndTargetType(employeeId, ReviewTargetType.EMPLOYEE);
            long totalReviews = allReviewsForEmployee.size();
            double averageRating = totalReviews > 0 ?
                    allReviewsForEmployee.stream()
                            .mapToDouble(Review::getRating)
                            .average()
                            .orElse(0.0) : 0.0;
            employee.setAverageRating(BigDecimal.valueOf(averageRating));
            employee.setTotalReviews(totalReviews);
            employeeRepository.save(employee);
        });
    }

    private void updateStoreServiceAverageRating(Integer storeServiceId) {
        storeServiceRepository.findById(storeServiceId).ifPresent(storeService -> {
            List<Review> allReviewsForStoreService = reviewRepository.findAllByTargetIdAndTargetType(storeServiceId, ReviewTargetType.STORE_SERVICE);
            long totalReviews = allReviewsForStoreService.size();
            double averageRating = totalReviews > 0 ?
                    allReviewsForStoreService.stream()
                            .mapToDouble(Review::getRating)
                            .average()
                            .orElse(0.0) : 0.0;
            storeService.setAverageRating(BigDecimal.valueOf(averageRating));
            storeService.setTotalReviews(totalReviews);
            storeServiceRepository.save(storeService);
        });
    }
    // KẾT THÚC THÊM CÁC PHƯƠNG THỨC NÀY
}