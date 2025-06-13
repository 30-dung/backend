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
        String subject = "Cuộc hẹn của bạn đã được xác nhận - BarberShop";
        String content = """
                <div style="padding: 20px; background-color: #f8f9fa; border-radius: 8px; margin: 20px 0;">
                    <h3 style="color: #15397F; margin-bottom: 20px;">🎉 Cuộc hẹn đã được xác nhận!</h3>
                    
                    <p style="font-size: 16px; margin-bottom: 15px;">Chào <strong>%s</strong>,</p>
                    
                    <p style="font-size: 16px; line-height: 1.6;">
                        Cuộc hẹn của bạn đã được nhân viên <strong style="color: #15397F;">%s</strong> xác nhận thành công!
                    </p>
                    
                    <div style="background-color: white; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #15397F;">
                        <p style="margin: 5px 0;"><strong>📅 Thời gian:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>💼 Dịch vụ:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>👨‍💼 Nhân viên:</strong> %s</p>
                    </div>
                    
                    <p style="font-size: 16px; color: #28a745; font-weight: bold;">
                        ✅ Vui lòng có mặt đúng giờ để được phục vụ tốt nhất!
                    </p>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        Nếu bạn cần thay đổi hoặc hủy cuộc hẹn, vui lòng liên hệ với chúng tôi trước ít nhất 2 giờ.
                    </p>
                </div>
                """.formatted(customerName, employeeName, timeRange, serviceName, employeeName);

        sendHtmlEmailWithLogo(to, subject, content);
    }

    @Async
    public void sendAppointmentCancellation(String to, String customerName, String employeeName, String timeRange, String serviceName) throws MessagingException, IOException {
        String subject = "Cuộc hẹn đã bị hủy - BarberShop";
        String content = """
                <div style="padding: 20px; background-color: #fff3cd; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107;">
                    <h3 style="color: #856404; margin-bottom: 20px;">⚠️ Thông báo hủy cuộc hẹn</h3>
                    
                    <p style="font-size: 16px; margin-bottom: 15px;">Chào <strong>%s</strong>,</p>
                    
                    <p style="font-size: 16px; line-height: 1.6;">
                        Rất tiếc, cuộc hẹn của bạn đã bị hủy:
                    </p>
                    
                    <div style="background-color: white; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>📅 Thời gian:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>💼 Dịch vụ:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>👨‍💼 Nhân viên:</strong> %s</p>
                    </div>
                    
                    <p style="font-size: 16px; color: #15397F;">
                        💡 Bạn có thể đặt lại cuộc hẹn khác bằng cách truy cập website của chúng tôi.
                    </p>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        Xin lỗi vì sự bất tiện này. Chúng tôi luôn sẵn sàng phục vụ bạn!
                    </p>
                </div>
                """.formatted(customerName, timeRange, serviceName, employeeName);

        sendHtmlEmailWithLogo(to, subject, content);
    }

    // NEW: Email thông báo cho nhân viên khi có cuộc hẹn mới
    @Async
    public void sendNewAppointmentNotificationToEmployee(String employeeEmail, String employeeName, String customerName, String timeRange, String serviceName, String customerPhone) throws MessagingException, IOException {
        String subject = "🔔 Bạn có cuộc hẹn mới - BarberShop";
        String content = """
                <div style="padding: 20px; background-color: #e8f4fd; border-radius: 8px; margin: 20px 0; border-left: 4px solid #0066cc;">
                    <h3 style="color: #0066cc; margin-bottom: 20px;">🔔 Thông báo cuộc hẹn mới!</h3>
                    
                    <p style="font-size: 16px; margin-bottom: 15px;">Chào <strong>%s</strong>,</p>
                    
                    <p style="font-size: 16px; line-height: 1.6;">
                        Bạn có một cuộc hẹn mới từ khách hàng <strong style="color: #0066cc;">%s</strong>
                    </p>
                    
                    <div style="background-color: white; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #28a745;">
                        <p style="margin: 5px 0;"><strong>👤 Khách hàng:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>📞 Số điện thoại:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>📅 Thời gian:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>💼 Dịch vụ:</strong> %s</p>
                    </div>
                    
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107;">
                        <p style="margin: 0; font-size: 16px; color: #856404; font-weight: bold;">
                            ⏰ Trạng thái: <span style="color: #dc3545;">CHƯA XÁC NHẬN</span>
                        </p>
                        <p style="margin: 10px 0 0 0; font-size: 14px; color: #856404;">
                            Vui lòng vào hệ thống để xác nhận hoặc từ chối cuộc hẹn này.
                        </p>
                    </div>
                    
                    <div style="text-align: center; margin: 25px 0;">
                        <a href="http://yourdomain.com/employee/appointments" 
                           style="background-color: #15397F; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                            🔗 Xem chi tiết cuộc hẹn
                        </a>
                    </div>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        💡 <strong>Lưu ý:</strong> Hãy xác nhận cuộc hẹn sớm để khách hàng có thể chuẩn bị tốt nhất.
                    </p>
                </div>
                """.formatted(employeeName, customerName, customerName, customerPhone != null ? customerPhone : "Chưa cung cấp", timeRange, serviceName);

        sendHtmlEmailWithLogo(employeeEmail, subject, content);
    }

    // NEW: Email thông báo cho nhân viên khi khách hàng hủy cuộc hẹn
    @Async
    public void sendAppointmentCancellationToEmployee(String employeeEmail, String employeeName, String customerName, String timeRange, String serviceName) throws MessagingException, IOException {
        String subject = "❌ Cuộc hẹn đã bị hủy - BarberShop";
        String content = """
                <div style="padding: 20px; background-color: #f8d7da; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                    <h3 style="color: #721c24; margin-bottom: 20px;">❌ Thông báo hủy cuộc hẹn</h3>
                    
                    <p style="font-size: 16px; margin-bottom: 15px;">Chào <strong>%s</strong>,</p>
                    
                    <p style="font-size: 16px; line-height: 1.6;">
                        Khách hàng <strong style="color: #721c24;">%s</strong> đã hủy cuộc hẹn sau:
                    </p>
                    
                    <div style="background-color: white; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>👤 Khách hàng:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>📅 Thời gian:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>💼 Dịch vụ:</strong> %s</p>
                    </div>
                    
                    <p style="font-size: 16px; color: #28a745;">
                        ✅ Khung giờ này hiện đã được giải phóng và có thể nhận cuộc hẹn khác.
                    </p>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        Bạn có thể kiểm tra lịch làm việc để xem các cuộc hẹn còn lại trong ngày.
                    </p>
                </div>
                """.formatted(employeeName, customerName, customerName, timeRange, serviceName);

        sendHtmlEmailWithLogo(employeeEmail, subject, content);
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
                    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 20px;">
                        <div style="max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                            <div style="text-align: center; background: linear-gradient(135deg, #15397F 0%%, #1e4a8c 100%%); padding: 30px 20px; color: white;">
                                <img src="cid:barbershopImage" alt="BarberShop Logo" style="max-width: 120px; border-radius: 10px; margin-bottom: 15px;" />
                                <h1 style="margin: 0; font-size: 28px; font-weight: bold;">BarberShop</h1>
                                <p style="margin: 5px 0 0 0; font-size: 16px; opacity: 0.9;">Hệ thống của 30 Dark</p>
                            </div>
                            
                            <div style="padding: 30px 20px;">
                                %s
                            </div>
                            
                            <div style="background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; border-top: 1px solid #e9ecef;">
                                <p style="margin: 0 0 10px 0; font-weight: bold; color: #15397F;">Cảm ơn bạn đã tin tưởng BarberShop!</p>
                                <p style="margin: 5px 0; font-size: 14px;">📍 <strong>Địa chỉ:</strong> 123 Đường ABC, Quận XYZ</p>
                                <p style="margin: 5px 0; font-size: 14px;">📞 <strong>Hotline:</strong> 0384 804 325</p>
                                <p style="margin: 5px 0; font-size: 14px;">🌐 <strong>Website:</strong> <a href="http://yourdomain.com" style="color: #15397F; text-decoration: none;">yourdomain.com</a></p>
                            </div>
                        </div>
                    </body>
                </html>
                """.formatted(content);
    }

    public void sendAppointmentCompletion(String email, String customerName, String employeeName, String timeRange, String serviceName) {
        // Implementation for appointment completion email
    }
}