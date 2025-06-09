package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.User;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.security.JwtUtil;
import com.example.serversideclinet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
public class UserController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder; // Đảm bảo autowire PasswordEncoder

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!jwtUtil.validateToken(token, userDetails)) {
            return ResponseEntity.status(401).body(new UserController.ErrorResponse("Invalid token"));
        }

        String email = jwtUtil.getUsernameFromToken(token);
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body(new UserController.ErrorResponse("User not found"));
        }

        return ResponseEntity.ok(new UserController.UserProfileResponse(
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getMembershipType().toString(),
                user.getLoyaltyPoints()
        ));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authorizationHeader,
                                           @RequestBody UpdateProfileRequest request) {
        String token = authorizationHeader.replace("Bearer ", "");
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!jwtUtil.validateToken(token, userDetails)) {
            return ResponseEntity.status(401).body(new UserController.ErrorResponse("Invalid token"));
        }

        String email = jwtUtil.getUsernameFromToken(token);
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body(new UserController.ErrorResponse("User not found"));
        }

        // Cập nhật fullName và phoneNumber
        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Cập nhật mật khẩu nếu có yêu cầu
        if (request.getCurrentPassword() != null && !request.getCurrentPassword().isEmpty()) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.status(400).body(new ErrorResponse("Current password is incorrect"));
            }

            if (request.getNewPassword() == null || request.getNewPassword().isEmpty() ||
                    !request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.status(400).body(new ErrorResponse("New passwords do not match or are empty"));
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userService.saveUser(user);
        return ResponseEntity.ok(new UserController.UserProfileResponse(
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getMembershipType().toString(),
                user.getLoyaltyPoints()
        ));
    }

    public static class UserProfileResponse {
        private String fullName;
        private String email;
        private String phoneNumber;
        private String membershipType;
        private Integer loyaltyPoints;

        public UserProfileResponse(String fullName, String email, String phoneNumber, String membershipType, Integer loyaltyPoints) {
            this.fullName = fullName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.membershipType = membershipType;
            this.loyaltyPoints = loyaltyPoints;
        }

        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getMembershipType() { return membershipType; }
        public Integer getLoyaltyPoints() { return loyaltyPoints; }
    }

    public static class UpdateProfileRequest {
        private String fullName;
        private String phoneNumber;
        private String currentPassword; // Thêm trường này
        private String newPassword;     // Thêm trường này
        private String confirmPassword; // Thêm trường này

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}