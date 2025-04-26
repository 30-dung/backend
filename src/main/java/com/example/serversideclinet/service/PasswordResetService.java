package com.example.serversideclinet.service;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.PasswordResetToken;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.PasswordResetTokenRepository;
import com.example.serversideclinet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void sendOtp(String email) throws MessagingException {
        User user = userRepository.findByEmail(email).orElse(null);
        Employee employee = employeeRepository.findByEmail(email).orElse(null);

        if (user == null && employee == null) {
            throw new RuntimeException("No user or employee found with email: " + email);
        }


        String otp = generateOtp();


        PasswordResetToken token = new PasswordResetToken(
                otp,
                user,
                employee,
                LocalDateTime.now().plusMinutes(10) // OTP hết hạn sau 10 phút
        );
        tokenRepository.save(token);


        sendEmail(email, otp);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("OTP has expired");
        }

        if (resetToken.getUser() != null) {
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else if (resetToken.getEmployee() != null) {
            Employee employee = resetToken.getEmployee();
            employee.setPassword(passwordEncoder.encode(newPassword));
            employeeRepository.save(employee);
        } else {
            throw new RuntimeException("Invalid OTP");
        }


        tokenRepository.delete(resetToken);
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void sendEmail(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Password Reset OTP");
        helper.setText("Your OTP for password reset is: <b>" + otp + "</b>. It is valid for 10 minutes.", true);

        mailSender.send(message);
    }
}