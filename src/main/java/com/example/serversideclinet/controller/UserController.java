package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.User;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.security.JwtUtil;
import com.example.serversideclinet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
public class UserController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

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

        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
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

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}