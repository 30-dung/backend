package com.example.serversideclinet.service;

import com.example.serversideclinet.model.Notification;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationRepository notificationRepository;

    public void sendEmail(String to, String subject, String content) {
        try {
            emailService.sendInvoiceEmail(to, subject, content);
        } catch (MessagingException | java.io.IOException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
        }
    }

    public void sendNotificationToUser(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    public void markNotificationAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void sendAppointmentConfirmation(User user, String appointmentDetails) {
        // Gửi email xác nhận lịch hẹn
        String subject = "Xác nhận đặt lịch - BarberShop";
        String content = "<p>Kính gửi " + user.getFullName() + ",</p>" +
                "<p>Cảm ơn bạn đã đặt lịch tại <strong>BarberShop</strong>.</p>" +
                "<p>Chi tiết lịch hẹn của bạn:</p>" +
                "<div style='padding: 10px; border: 1px solid #ddd; border-radius: 5px;'>" +
                appointmentDetails +
                "</div>" +
                "<p>Vui lòng có mặt trước 5-10 phút để không bỏ lỡ lịch hẹn của bạn.</p>";

        sendEmail(user.getEmail(), subject, content);

        // Lưu thông báo vào hệ thống
        sendNotificationToUser(user, "Bạn đã đặt lịch hẹn thành công. " + appointmentDetails);
    }
}