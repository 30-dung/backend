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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

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

    // UPDATED & RENAMED: Phương thức gửi email khi cuộc hẹn bị hủy cho KHÁCH HÀNG
    // Thêm tham số 'canceledBy'
    @Async
    public void sendAppointmentCancellationToCustomer(String customerEmail, String customerName, String employeeName, String timeRange, String serviceName, String canceledBy) throws MessagingException, IOException {
        String subject = "Cuộc hẹn đã bị hủy - BarberShop";
        String content = String.format(
                """
                <div style="padding: 20px; background-color: #f8d7da; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                    <h3 style="color: #721c24; margin-bottom: 20px;">❌ Thông báo hủy cuộc hẹn</h3>
                    
                    <p style="font-size: 16px; margin-bottom: 15px;">Chào <strong>%s</strong>,</p>
                    
                    <p style="font-size: 16px; line-height: 1.6;">
                        Rất tiếc, cuộc hẹn dịch vụ <b>%s</b> của bạn với <b>%s</b> vào lúc <b>%s</b> đã bị <b>hủy bỏ</b>.
                    </p>
                    
                    <p style="font-size: 16px; color: #721c24; font-weight: bold;">
                        Người hủy: %s
                    </p>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        Vui lòng liên hệ với chúng tôi để sắp xếp lại lịch hẹn hoặc biết thêm chi tiết.
                    </p>
                </div>
                """, customerName, serviceName, employeeName, timeRange, canceledBy
        );
        sendHtmlEmailWithLogo(customerEmail, subject, content);
    }

    // NEW: Email thông báo cho nhân viên khi có cuộc hẹn mới (đã có trong file bạn cung cấp)
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

    // Original: sendAppointmentCancellationToEmployee
    // Đây là phương thức gửi cho nhân viên khi KHÁCH HÀNG hủy.
    // Bạn đã có nó, tôi sẽ giữ nguyên nhưng tách riêng ra để rõ ràng hơn.
    @Async
    public void sendAppointmentCancellationToEmployee(String employeeEmail, String employeeName, String customerName, String timeRange, String serviceName) throws MessagingException, IOException {
        String subject = "❌ Cuộc hẹn của khách hàng đã bị hủy - BarberShop";
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

    // NEW: Phương thức gửi email khi lịch bị từ chối (customer)
    @Async
    public void sendAppointmentRejection(String customerEmail, String customerName, String employeeName, String startTime, String serviceName, String reason) throws MessagingException, IOException {
        String subject = "Thông báo: Lịch hẹn của bạn đã bị từ chối - BarberShop";
        String content = String.format(
                """
                <div style="padding: 20px; background-color: #f8d7da; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                    <h3 style="color: #721c24; margin-bottom: 20px;">❌ Cuộc hẹn của bạn đã bị từ chối</h3>
                    
                    <p style="font-size: 16px; margin-bottom: 15px;">Chào <strong>%s</strong>,</p>
                    
                    <p style="font-size: 16px; line-height: 1.6;">
                        Chúng tôi rất tiếc phải thông báo rằng lịch hẹn dịch vụ <b>%s</b> của bạn với <b>%s</b> vào lúc <b>%s</b> đã bị <b>từ chối</b>.
                    </p>
                    
                    <p style="font-size: 16px; color: #721c24; font-weight: bold;">
                        Lý do: %s
                    </p>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        Bạn có thể đặt lại lịch hẹn khác hoặc liên hệ với chúng tôi để được hỗ trợ thêm.
                    </p>
                </div>
                """, customerName, serviceName, employeeName, startTime, reason
        );
        sendHtmlEmailWithLogo(customerEmail, subject, content);
    }

    // NEW: Phương thức gửi email khi lịch được chuyển (cho khách hàng)
    @Async
    public void sendAppointmentReassignment(String customerEmail, String customerName, String oldEmployeeName, String newEmployeeName, String startTime, String serviceName) throws MessagingException, IOException {
        String subject = "Lịch hẹn của bạn đã được chuyển - BarberShop";
        String content = String.format(
                """
                <div style="padding: 20px; background-color: #e6f7ff; border-radius: 8px; margin: 20px 0; border-left: 4px solid #1890ff;">
                    <h3 style="color: #096dd9; margin-bottom: 20px;">🔄 Lịch hẹn của bạn đã được chuyển</h3>
                    
                    <p style="font-size: 16px; margin-bottom: 15px;">Chào <strong>%s</strong>,</p>
                    
                    <p style="font-size: 16px; line-height: 1.6;">
                        Vì một số lý do không mong muốn, lịch hẹn dịch vụ <b>%s</b> của bạn vào lúc <b>%s</b>,
                        ban đầu với <b>%s</b>, đã được <b>chuyển sang cho <b>%s</b></b>.
                    </p>
                    
                    <p style="font-size: 16px; color: #096dd9; font-weight: bold;">
                        Chúng tôi xin lỗi vì sự bất tiện này. Lịch hẹn của bạn vẫn được giữ nguyên về thời gian và dịch vụ.
                    </p>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        Chúng tôi mong được đón tiếp bạn!
                    </p>
                </div>
                """, customerName, serviceName, startTime, oldEmployeeName, newEmployeeName
        );
        sendHtmlEmailWithLogo(customerEmail, subject, content);
    }

    // NEW: Phương thức gửi email thông báo lịch được chuyển cho nhân viên mới
    @Async
    public void sendReassignedAppointmentNotificationToEmployee(String newEmployeeEmail, String newEmployeeName, String customerName, String timeRange, String serviceName, String customerPhone, String oldEmployeeName) throws MessagingException, IOException {
        String subject = "Bạn có lịch hẹn được phân công lại - BarberShop";
        String content = String.format(
                """
                <div style="padding: 20px; background-color: #e0ffe0; border-radius: 8px; margin: 20px 0; border-left: 4px solid #00c000;">
                    <h3 style="color: #008000; margin-bottom: 20px;">✅ Lịch hẹn được phân công lại cho bạn</h3>
                    
                    <p style="font-size: 16px; margin-bottom: 15px;">Chào <strong>%s</strong>,</p>
                    
                    <p style="font-size: 16px; line-height: 1.6;">
                        Bạn vừa được phân công lại một lịch hẹn:
                    </p>
                    
                    <div style="background-color: white; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #008000;">
                        <p style="margin: 5px 0;"><strong>👤 Khách hàng:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>📞 Số điện thoại:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>📅 Thời gian:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>💼 Dịch vụ:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Ban đầu bởi:</strong> %s</p>
                    </div>
                    
                    <p style="font-size: 16px; color: #008000; font-weight: bold;">
                        Vui lòng vào hệ thống để xác nhận lịch hẹn này.
                    </p>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        Cảm ơn sự hợp tác của bạn!
                    </p>
                </div>
                """, newEmployeeName, customerName, customerPhone != null ? customerPhone : "Chưa cung cấp", timeRange, serviceName, oldEmployeeName
        );
        sendHtmlEmailWithLogo(newEmployeeEmail, subject, content);
    }

    @Async
    public void sendFeedbackReply(String to, String customerName, String originalComment, String replyContent) throws MessagingException, IOException {
        String subject = "Phản hồi về góp ý của bạn - BarberShop";
        String content = String.format(
                """
                <div style="padding: 20px; background-color: #f0f8ff; border-radius: 8px; margin: 20px 0; border-left: 4px solid #4CAF50;">
                    <h3 style="color: #2196F3; margin-bottom: 20px;">✉️ Phản hồi từ BarberShop</h3>
                    
                    <p style="font-size: 16px; margin-bottom: 15px;">Chào <strong>%s</strong>,</p>
                    
                    <p style="font-size: 16px; line-height: 1.6;">
                        Chúng tôi đã nhận được góp ý của bạn và xin gửi lời phản hồi như sau:
                    </p>
                    
                    <div style="background-color: #e9e9e9; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ccc;">
                        <p style="margin: 5px 0; font-weight: bold;">Góp ý của bạn:</p>
                        <p style="margin: 5px 0;">"%s"</p>
                    </div>
                    
                    <div style="background-color: white; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #4CAF50;">
                        <p style="margin: 5px 0; font-weight: bold; color: #4CAF50;">Phản hồi từ chúng tôi:</p>
                        <p style="margin: 5px 0;">"%s"</p>
                    </div>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        Cảm ơn bạn đã tin tưởng và gửi góp ý cho chúng tôi!
                    </p>
                </div>
                """, customerName, originalComment, replyContent
        );
        sendHtmlEmailWithLogo(to, subject, content);
    }

    // sendAppointmentCompletion (tồn tại trong file bạn cung cấp, không thay đổi)
    public void sendAppointmentCompletion(String email, String customerName, String employeeName, String timeRange, String serviceName) {
        // Implementation for appointment completion email
        logger.info("Completion email placeholder called for {}", email);
    }
}