package com.example.serversideclinet.service;

import com.example.serversideclinet.model.User;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void sendEmail(String to, String subject, String content) {
        // Tích hợp JavaMailSender hoặc sử dụng API email bên thứ 3
    }

    public void sendNotificationToUser(User user, String message) {
        // Gửi notification (nếu có Notification table hoặc gửi về frontend)
    }
}
