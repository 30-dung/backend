package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.Feedback;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.service.FeedbackService;
import com.example.serversideclinet.service.UserService; // Import UserService
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService; // Inject UserService

    @PostMapping("/submit")
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String email = payload.get("email");
        String comment = payload.get("comment");

        // Nếu người dùng đã xác thực, thử điền trước tên và email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String loggedInEmail = userDetails.getUsername(); // Email thường là username trong UserDetails
            User user = userService.getUserByEmail(loggedInEmail); // Lấy user từ UserService
            if (user != null) {
                name = user.getFullName(); // Lấy full name từ User model
                email = user.getEmail(); // Lấy email từ User model
            }
        }

        if (name == null || name.isEmpty() || email == null || email.isEmpty() || comment == null || comment.isEmpty()) {
            return ResponseEntity.badRequest().body("Tên, email và nội dung góp ý không được để trống.");
        }

        Feedback feedback = feedbackService.submitFeedback(name, email, comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(feedback);
    }

    @GetMapping
    // Bạn có thể thêm @PreAuthorize("hasRole('ADMIN')") ở đây nếu muốn chỉ admin mới xem được
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        List<Feedback> feedbackList = feedbackService.getAllFeedback();
        return ResponseEntity.ok(feedbackList);
    }

    @PostMapping("/{id}/reply")
    // Bạn có thể thêm @PreAuthorize("hasRole('ADMIN')") ở đây nếu muốn chỉ admin mới có quyền trả lời
    public ResponseEntity<?> replyToFeedback(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String replyContent = payload.get("replyContent");
        if (replyContent == null || replyContent.isEmpty()) {
            return ResponseEntity.badRequest().body("Nội dung phản hồi không được để trống.");
        }
        try {
            Feedback updatedFeedback = feedbackService.replyToFeedback(id, replyContent);
            if (updatedFeedback != null) {
                return ResponseEntity.ok(updatedFeedback);
            }
            return ResponseEntity.notFound().build();
        } catch (MessagingException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi gửi email phản hồi: " + e.getMessage());
        }
    }
}