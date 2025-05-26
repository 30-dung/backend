package com.example.serversideclinet.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendAppointmentConfirmation(String to, String customerName, String employeeName, String timeRange, String serviceName) throws MessagingException, IOException {
        String subject = "Xác nhận cuộc hẹn thành công";
        String content = """
                <p>Chào <strong>%s</strong>,</p>
                <p>Cuộc hẹn của bạn với nhân viên <strong>%s</strong> cho dịch vụ <strong>%s</strong> vào thời gian <strong>%s</strong> đã được xác nhận.</p>
                <p>Hẹn gặp bạn tại cửa hàng!</p>
                """.formatted(customerName, employeeName, serviceName, timeRange);

        sendHtmlEmailWithLogo(to, subject, content);
    }

    public void sendAppointmentCancellation(String to, String customerName, String employeeName, String timeRange, String serviceName) throws MessagingException, IOException {
        String subject = "Cuộc hẹn bị hủy";
        String content = """
                <p>Chào <strong>%s</strong>,</p>
                <p>Cuộc hẹn với nhân viên <strong>%s</strong> cho dịch vụ <strong>%s</strong> vào thời gian <strong>%s</strong> đã bị hủy.</p>
                <p>Nếu đây là sự nhầm lẫn, bạn có thể đặt lại cuộc hẹn.</p>
                """.formatted(customerName, employeeName, serviceName, timeRange);

        sendHtmlEmailWithLogo(to, subject, content);
    }

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
                    <head>
                        <style>
                            body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                            .container { max-width: 600px; margin: auto; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
                            .header { text-align: center; }
                            .footer { margin-top: 30px; font-size: 12px; color: #888; text-align: center; }
                            .button {
                                display: inline-block;
                                padding: 10px 20px;
                                background-color: #007bff;
                                color: white;
                                text-decoration: none;
                                border-radius: 5px;
                                margin-top: 20px;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <img src="cid:barbershopImage" alt="BarberShop" width="150" />
                                <h2>BarberShop - Hệ thống của 30 Dark</h2>
                            </div>
                            <div class="body">
                                %s
                            </div>
                            <div class="footer">
                                Cảm ơn bạn đã sử dụng dịch vụ của <strong>BarberShop</strong> – thuộc công ty <strong>30 Dark</strong>.<br/>
                                Địa chỉ: 123 Đường ABC, Quận XYZ<br/>
                                Hotline: <strong>0384 804 325</strong><br/>
                                Website: <a href="https://yourdomain.com">yourdomain.com</a>
                            </div>
                        </div>
                    </body>
                </html>
                """.formatted(content);
    }
}
