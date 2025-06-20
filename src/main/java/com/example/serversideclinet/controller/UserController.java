// src/main/java/com/example/serversideclinet/controller/UserController.java
package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.UserProfileResponse;
import com.example.serversideclinet.model.MembershipType;
import com.example.serversideclinet.model.Role;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.security.JwtUtil;
import com.example.serversideclinet.service.UserService;
import com.example.serversideclinet.repository.RoleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequestMapping("api/user")
public class UserController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!jwtUtil.validateToken(token, userDetails)) {
            return ResponseEntity.status(401).body(new ErrorResponse("Invalid token"));
        }

        String email = jwtUtil.getUsernameFromToken(token);
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
        }

        return ResponseEntity.ok(new UserProfileResponse(
                user.getUserId(),
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
            return ResponseEntity.status(401).body(new ErrorResponse("Invalid token"));
        }

        String email = jwtUtil.getUsernameFromToken(token);
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body(new ErrorResponse("User not found"));
        }

        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

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
        return ResponseEntity.ok(new UserProfileResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getMembershipType().toString(),
                user.getLoyaltyPoints()
        ));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customer/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        Optional<User> userOptional = userService.getUserById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return ResponseEntity.ok(new UserProfileResponse(
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getMembershipType().toString(),
                    user.getLoyaltyPoints()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("User not found")); //
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/customer/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Email already exists"));
        }

        User newUser = new User();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Encode password
        newUser.setMembershipType(MembershipType.REGULAR); // Default to REGULAR
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setLoyaltyPoints(0);

        // Assign 'CUSTOMER' role by default
        Optional<Role> customerRole = roleRepository.findByRoleName("CUSTOMER");
        if (customerRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("CUSTOMER role not found"));
        }
        newUser.setRoles(Collections.singleton(customerRole.get()));

        User savedUser = userService.saveUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserProfileResponse(
                savedUser.getUserId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedUser.getMembershipType().toString(),
                savedUser.getLoyaltyPoints()
        ));
    }

    @PutMapping("/custome/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UpdateUserAdminRequest request) {
        Optional<User> existingUserOptional = userService.getUserById(id);
        if (existingUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("User not found"));
        }

        User existingUser = existingUserOptional.get();

        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            existingUser.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            existingUser.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // Check if new email already exists for another user
            Optional<User> userWithNewEmail = userService.findByEmail(request.getEmail());
            if (userWithNewEmail.isPresent() && !userWithNewEmail.get().getUserId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Email already in use by another user"));
            }
            existingUser.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getMembershipType() != null && !request.getMembershipType().isEmpty()) {
            try {
                existingUser.setMembershipType(MembershipType.valueOf(request.getMembershipType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Invalid membership type"));
            }
        }
        if (request.getLoyaltyPoints() != null) {
            existingUser.setLoyaltyPoints(request.getLoyaltyPoints());
        }

        User updatedUser = userService.saveUser(existingUser);
        return ResponseEntity.ok(new UserProfileResponse(
                updatedUser.getUserId(),
                updatedUser.getFullName(),
                updatedUser.getEmail(),
                updatedUser.getPhoneNumber(),
                updatedUser.getMembershipType().toString(),
                updatedUser.getLoyaltyPoints()
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/customer/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        if (!userService.getUserById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("User not found"));
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }


    // DTOs for requests and responses
    public static class UpdateProfileRequest {
        private String fullName;
        private String phoneNumber;
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

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

    public static class CreateUserRequest {
        private String fullName;
        private String email;
        private String phoneNumber;
        private String password;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UpdateUserAdminRequest {
        private String fullName;
        private String email;
        private String phoneNumber;
        private String password;
        private String membershipType;
        private Integer loyaltyPoints;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getMembershipType() { return membershipType; }
        public void setMembershipType(String membershipType) { this.membershipType = membershipType; }
        public Integer getLoyaltyPoints() { return loyaltyPoints; }
        public void setLoyaltyPoints(Integer loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    }


    public static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class MessageResponse {
        private String message;
        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}