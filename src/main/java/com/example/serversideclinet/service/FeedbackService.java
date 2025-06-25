package com.example.serversideclinet.service;

import com.example.serversideclinet.model.Feedback;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.repository.FeedbackRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService; // Sử dụng UserService đã cung cấp

    public Feedback submitFeedback(String name, String email, String comment) {
        Feedback feedback = new Feedback(name, email, comment);
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public Optional<Feedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    public Feedback replyToFeedback(Long feedbackId, String replyContent) throws MessagingException, IOException {
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(feedbackId);
        if (optionalFeedback.isPresent()) {
            Feedback feedback = optionalFeedback.get();
            feedback.setReply(replyContent);
            feedback.setRepliedAt(LocalDateTime.now());
            Feedback updatedFeedback = feedbackRepository.save(feedback);

            // Gửi email phản hồi cho người dùng sử dụng phương thức mới trong EmailService
            emailService.sendFeedbackReply(feedback.getEmail(), feedback.getName(), feedback.getComment(), replyContent);

            return updatedFeedback;
        }
        return null; // Hoặc ném một ngoại lệ cụ thể (e.g., ResourceNotFoundException)
    }
}