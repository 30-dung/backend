package com.example.serversideclinet.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendAppointmentConfirmation(String to, String customerName, String employeeName, String timeRange, String serviceName) throws MessagingException, IOException {
        String subject = "Xác nhận cuộc hẹn thành công";
        String content = """
                Chào %s,

                Cuộc hẹn của bạn với nhân viên %s cho dịch vụ %s vào thời gian %s đã được xác nhận.

                Hẹn gặp bạn tại cửa hàng!

                """.formatted(customerName, employeeName, serviceName, timeRange);

        sendHtmlEmailWithLogo(to, subject, content);
    }

    @Async
    public void sendAppointmentCancellation(String to, String customerName, String employeeName, String timeRange, String serviceName) throws MessagingException, IOException {
        String subject = "Cuộc hẹn bị hủy";
        String content = """
                Chào %s,

                Cuộc hẹn với nhân viên %s cho dịch vụ %s vào thời gian %s đã bị hủy.

                Nếu đây là sự nhầm lẫn, bạn có thể đặt lại cuộc hẹn.

                """.formatted(customerName, employeeName, serviceName, timeRange);

        sendHtmlEmailWithLogo(to, subject, content);
    }

    @Async
    public void sendInvoiceEmail(String to, String subject, String bodyContent) throws MessagingException, IOException {
        sendHtmlEmailWithLogo(to, subject, bodyContent);
    }


    private void sendHtmlEmailWithLogo(String to, String subject, String htmlBody) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        String html = buildHtmlEmail(htmlBody);
        helper.setText(html, true);

        // Thêm ảnh logo trong resources (src/main/resources/static/images/barbershop.jpg)
        ClassPathResource image = new ClassPathResource("static/images/barbershop.jpg");
        helper.addInline("barbershopImage", image);

        mailSender.send(message);
    }

    private String buildHtmlEmail(String content) {
        return """
                <html>
                    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                        <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                            <div style="text-align: center;">
                                <img src="cid:barbershopImage" alt="BarberShop Logo" style="max-width: 150px;" />
                                <h2 style="color: #15397F;">BarberShop - Hệ thống của 30 Dark</h2>
                            </div>
                            <div style="margin: 20px 0;">
                                %s
                            </div>
                            <div style="text-align: center; color: #777;">
                                <p>Cảm ơn bạn đã sử dụng dịch vụ của BarberShop – thuộc công ty 30 Dark.</p>
                                <p>Địa chỉ: 123 Đường ABC, Quận XYZ</p>
                                <p>Hotline: 0384 804 325</p>
                                <p>Website: <a href="http://yourdomain.com">yourdomain.com</a></p>
                            </div>
                        </div>
                    </body>
                </html>
                """.formatted(content);
    }
}