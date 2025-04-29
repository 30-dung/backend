package com.example.serversideclinet.service;

import com.example.serversideclinet.model.PasswordResetToken;
import com.example.serversideclinet.model.User;
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
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void sendOtp(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));


        String otp = generateOtp();


        PasswordResetToken token = new PasswordResetToken(
                otp,
                user,
                LocalDateTime.now().plusMinutes(10)
        );
        tokenRepository.save(token);

        // Gửi email
        sendEmail(user.getEmail(), otp);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("OTP has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);


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
        helper.setSubject("FourShine - Password Reset OTP");

        String content = """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <h2 style="text-align: center; color: #4CAF50;">Password Reset Request</h2>
                    <p>Hello,</p>
                    <p>You have requested a password reset for your <strong>FourShine</strong> account.</p>
                    <p>Your OTP is:</p>
                    <div style="text-align: center; font-size: 24px; font-weight: bold; margin: 20px 0; color: #333;">%s</div>
                    <p>This OTP is valid for <strong>10 minutes</strong>.</p>
                    <p>If you did not request this, please ignore this email.</p>
                    <br>
                    <p>Best regards,<br>The FourShine Team</p>
                </div>
            </body>
            </html>
            """.formatted(otp);

        helper.setText(content, true);

        mailSender.send(message);
    }

}
