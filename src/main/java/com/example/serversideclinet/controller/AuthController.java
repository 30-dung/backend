// src/main/java/com/example/serversideclinet/controller/AuthController.java
package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.*;
import com.example.serversideclinet.model.MembershipType;
import com.example.serversideclinet.model.Role;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.RoleRepository;
import com.example.serversideclinet.repository.UserRepository;
import com.example.serversideclinet.security.CustomUserDetails; // Import CustomUserDetails
import com.example.serversideclinet.security.CustomUserDetailsService;
import com.example.serversideclinet.security.JwtUtil;
import com.example.serversideclinet.service.PasswordResetService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Cast UserDetails to CustomUserDetails để truy cập thông tin cụ thể
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(request.getEmail());
            String token = jwtUtil.generateToken(userDetails);

            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            // Lấy thông tin userId và fullName dựa trên loại người dùng (User hoặc Employee)
            Integer id = null;
            String fullName = null;

            if (userDetails.isEmployee()) {
                id = userDetails.getEmployeeId();
                // Lấy fullName từ employeeRepository
                fullName = employeeRepository.findByEmail(request.getEmail())
                        .map(employee -> employee.getFullName())
                        .orElse(null);
            } else {
                id = userDetails.getUserId();
                // Lấy fullName từ userRepository
                fullName = userRepository.findByEmail(request.getEmail())
                        .map(user -> user.getFullName())
                        .orElse(null);
            }

            // Sử dụng constructor mới với id và fullName
            return ResponseEntity.ok(new AuthResponse(token, role, id, fullName));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, "Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, null, "Login failed: " + e.getMessage()));
        }
    }

    // CHỈ GIỮ MỘT PHƯƠNG THỨC REGISTER NÀY
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, "Email already exists"));
        }

        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, "Email already exists as an employee"));
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setMembershipType(MembershipType.valueOf(request.getMembershipType()));
        user.setLoyaltyPoints(0);

        Optional<Role> customerRoleOpt = roleRepository.findByRoleName("ROLE_CUSTOMER");
        if (customerRoleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, null, "Role 'ROLE_CUSTOMER' not found in the system"));
        }
        Role customerRole = customerRoleOpt.get();
        user.setRoles(new HashSet<>(Collections.singletonList(customerRole)));
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Sau khi đăng ký thành công, trả về userId và fullName của user mới tạo
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, role, user.getUserId(), user.getFullName()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.sendOtp(request.getEmail());
            return ResponseEntity.ok("OTP sent to your email");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse(null, null, "Failed to send OTP"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null, e.getMessage()));
        }
    }
}