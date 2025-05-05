package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.Invoice;
import com.example.serversideclinet.model.InvoiceStatus;
import com.example.serversideclinet.repository.InvoiceRepository;
import com.example.serversideclinet.security.CustomUserDetails;
import com.example.serversideclinet.service.EmailService;
import com.example.serversideclinet.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private VnpayService vnpayService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @GetMapping("/create/{invoiceId}")
    public ResponseEntity<?> createPayment(@PathVariable Integer invoiceId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails,
                                           HttpServletRequest request) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy hóa đơn.");
        }

        Invoice invoice = invoiceOpt.get();
        if (!invoice.getUser().getUserId().equals(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn chỉ được thanh toán hóa đơn của mình.");
        }

        String paymentUrl = vnpayService.createPaymentUrl(invoice, request);
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> allParams) {
        boolean isValid = vnpayService.verifyPayment(allParams);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sai chữ ký bảo mật.");
        }

        Integer invoiceId = Integer.parseInt(allParams.get("vnp_TxnRef"));
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy hóa đơn.");
        }

        Invoice invoice = invoiceOpt.get();

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            return ResponseEntity.ok("Hóa đơn đã được thanh toán trước đó.");
        }

        String responseCode = allParams.get("vnp_ResponseCode");
        String transactionStatus = allParams.get("vnp_TransactionStatus");

        String userEmail = invoice.getUser().getEmail();

        if (!"00".equals(responseCode) || !"00".equals(transactionStatus)) {
            invoice.setStatus(InvoiceStatus.CANCELED);
            invoiceRepository.save(invoice);

            try {
                emailService.sendInvoiceEmail(
                        userEmail,
                        "Thanh toán thất bại - BarberShop",
                        """
                        <p>Xin chào,</p>
                        <p>Hóa đơn <strong>#%d</strong> không thể thanh toán do lỗi hoặc bị hủy.</p>
                        <p>Vui lòng thử lại hoặc liên hệ với chúng tôi để được hỗ trợ.</p>
                        <p>Trân trọng,<br/><strong>BarberShop - 30 Dark</strong></p>
                        """.formatted(invoice.getInvoiceId())
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ResponseEntity.ok("Giao dịch bị từ chối hoặc thất bại. Mã lỗi: " + responseCode);
        }

        // Thanh toán thành công
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        try {
            emailService.sendInvoiceEmail(
                    userEmail,
                    "Thanh toán thành công - BarberShop",
                    """
                    <p>Xin chào,</p>
                    <p>Hóa đơn <strong>#%d</strong> của bạn đã được thanh toán thành công.</p>
                    <p>Số tiền: <strong>%s VND</strong></p>
                    <p>Cảm ơn bạn đã tin tưởng sử dụng dịch vụ của chúng tôi.</p>
                    <p>Trân trọng,<br/><strong>BarberShop - 30 Dark</strong></p>
                    """.formatted(invoice.getInvoiceId(), invoice.getTotalAmount())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("Thanh toán thành công cho hóa đơn #" + invoice.getInvoiceId());
    }

}
