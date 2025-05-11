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

    public void sendInvoiceEmail(String to, String subject, String bodyContent) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);

        String html = buildHtmlEmail(bodyContent);
        helper.setText(html, true);

        // Thêm ảnh từ resources (ảnh ở src/main/resources/static/images/barbershop.jpg)
        ClassPathResource image = new ClassPathResource("static/images/barbershop.jpg");
        helper.addInline("barbershopImage", image);

        mailSender.send(message);
    }

    private String buildHtmlEmail(String content) {
        return """
                <html>
                    <head>
                        <style>
                            body { font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; }
                            .container { max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 8px; }
                            .header { text-align: center; }
                            .footer { margin-top: 30px; font-size: 12px; color: #888; text-align: center; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <img src="cid:barbershopImage" alt="BarberShop" width="150" />
                                <h2>BarberShop - Hệ thống của 30 Dark</h2>
                                <h3>Thông báo thanh toán</h3>
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
