//package com.example.serversideclinet.controller;
//
//import com.example.serversideclinet.model.*;
//import com.example.serversideclinet.repository.*;
//import com.example.serversideclinet.service.NotificationService;
//import com.example.serversideclinet.service.VnpayService;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/payment")
//public class VnpayCallbackController {
//
//    @Autowired
//    private InvoiceRepository invoiceRepository;
//
//    @Autowired
//    private AppointmentRepository appointmentRepository;
//
//    @Autowired
//    private PaymentHistoryRepository paymentHistoryRepository;
//
//    @Autowired
//    private NotificationService notificationService;
//
//    @Autowired
//    private ReviewScheduleRepository reviewScheduleRepository;
//
//    @Autowired
//    private VnpayService vnpayService;
//
//    @GetMapping("/return")
//    @Transactional
//    public String handleVnpayReturn(@RequestParam Map<String, String> params) {
//        // Bước 1: Xác thực chữ ký
//        String secureHash = params.get("vnp_SecureHash");
//        if (!vnpayService.validateSignature(params, secureHash)) {
//            return "Thanh toán thất bại: Sai chữ ký!";
//        }
//
//        String invoiceIdStr = params.get("vnp_TxnRef");
//        String responseCode = params.get("vnp_ResponseCode");
//
//        if (!responseCode.equals("00")) {
//            return "Thanh toán thất bại từ VNPAY!";
//        }
//
//        int invoiceId = Integer.parseInt(invoiceIdStr);
//        Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceId);
//
//        if (optionalInvoice.isEmpty()) return "Không tìm thấy hóa đơn";
//
//        Invoice invoice = optionalInvoice.get();
//
//        // Bước 2: Cập nhật trạng thái hóa đơn
//        invoice.setStatus(InvoiceStatus.PAID);
//        invoiceRepository.save(invoice);
//
//        // Bước 3: Xác nhận lịch hẹn liên quan
//        for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
//            Appointment appointment = detail.getAppointment();
//            if (appointment != null && appointment.getStatus() == Appointment.Status.PENDING) {
//                appointment.setStatus(Appointment.Status.CONFIRMED);
//                appointmentRepository.save(appointment);
//
//                // Bước 4: Lên lịch đánh giá
//                ReviewSchedule review = new ReviewSchedule();
//                review.setUser(invoice.getUser());
//                review.setAppointment(appointment);
//                review.setScheduledAt(LocalDateTime.now().plusDays(2));
//                reviewScheduleRepository.save(review);
//            }
//        }
//
//        // Bước 5: Gửi thông báo
//        notificationService.sendEmail(
//                invoice.getUser().getEmail(),
//                "Thanh toán thành công",
//                "Bạn đã thanh toán thành công cho hóa đơn #" + invoice.getInvoiceId()
//        );
//
//        notificationService.sendNotificationToUser(
//                invoice.getUser(),
//                "Thanh toán thành công cho lịch hẹn #" + invoice.getInvoiceId()
//        );
//
//        // Bước 6: Lưu lịch sử thanh toán
//        PaymentHistory history = new PaymentHistory();
//        history.setInvoice(invoice);
//        history.setAmount(invoice.getTotalAmount());
//        history.setPaidAt(LocalDateTime.now());
//        history.setTransactionId(params.get("vnp_TransactionNo"));
//        history.setPaymentMethod("VNPAY");
//        paymentHistoryRepository.save(history);
//
//        return "Thanh toán thành công! Cảm ơn bạn đã sử dụng dịch vụ.";
//    }
//}
