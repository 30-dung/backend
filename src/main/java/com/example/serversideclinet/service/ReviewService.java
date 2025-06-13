package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.MultiReviewRequestDTO;
import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository;


    public List<Review> createMultiReviewsFromInvoice(MultiReviewRequestDTO request, User user) {
        Integer invoiceId = request.getInvoiceId();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (invoice.getStatus() != Invoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Invoice is not paid yet");
        }

        List<Review> reviews = new ArrayList<>();
        Set<Integer> reviewedStoreIds = new HashSet<>();
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceInvoiceId(invoiceId);

        for (InvoiceDetail detail : details) {
            Appointment appointment = detail.getAppointment();

            // SERVICE review
            Integer serviceId = appointment.getStoreService().getService().getServiceId();
            if (!reviewRepository.existsByUserIdAndTargetTypeAndTargetId(user.getUserId(), ReviewTargetType.SERVICE, serviceId)) {
                Review serviceReview = new Review();
                serviceReview.setUser(user);
                serviceReview.setTargetType(ReviewTargetType.SERVICE);
                serviceReview.setTargetId(serviceId);
                serviceReview.setRating(request.getServiceRating());
                serviceReview.setComment(request.getComment());
                serviceReview.setInvoiceDetail(detail);
                reviews.add(reviewRepository.save(serviceReview));
            }

            // EMPLOYEE review
            Integer employeeId = appointment.getEmployee().getEmployeeId();
            if (!reviewRepository.existsByUserIdAndTargetTypeAndTargetId(user.getUserId(), ReviewTargetType.EMPLOYEE, employeeId)) {
                Review employeeReview = new Review();
                employeeReview.setUser(user);
                employeeReview.setTargetType(ReviewTargetType.EMPLOYEE);
                employeeReview.setTargetId(employeeId);
                employeeReview.setRating(request.getEmployeeRating());
                employeeReview.setComment(request.getComment());
                employeeReview.setInvoiceDetail(detail);
                reviews.add(reviewRepository.save(employeeReview));
            }

            // Lưu storeId để xử lý sau
            reviewedStoreIds.add(appointment.getStoreService().getStore().getStoreId());
        }

        // STORE review
        for (Integer storeId : reviewedStoreIds) {
            if (!reviewRepository.existsByUserIdAndTargetTypeAndTargetId(user.getUserId(), ReviewTargetType.STORE, storeId)) {
                Review storeReview = new Review();
                storeReview.setUser(user);
                storeReview.setTargetType(ReviewTargetType.STORE);
                storeReview.setTargetId(storeId);
                storeReview.setRating(request.getStoreRating());
                storeReview.setComment(request.getComment());

                // Tìm một invoiceDetail cùng store
                InvoiceDetail anyDetailForStore = details.stream()
                        .filter(d -> d.getAppointment().getStoreService().getStore().getStoreId().equals(storeId))
                        .findFirst()
                        .orElse(null);

                storeReview.setInvoiceDetail(anyDetailForStore); // Optional
                reviews.add(reviewRepository.save(storeReview));
            }
        }


        return reviews;
    }



    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
