package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.Invoice;
import com.example.serversideclinet.model.InvoiceStatus;
import com.example.serversideclinet.repository.InvoiceRepository;
import com.example.serversideclinet.security.CustomUserDetails;
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
        String responseCode = allParams.get("vnp_ResponseCode");
        String transactionStatus = allParams.get("vnp_TransactionStatus");

        if (!"00".equals(responseCode) || !"00".equals(transactionStatus)) {
            // Nếu giao dịch bị hủy/thất bại → cập nhật trạng thái
            invoice.setStatus(InvoiceStatus.CANCELED); // Bạn nên có enum này
            invoiceRepository.save(invoice);
            return ResponseEntity.status(HttpStatus.OK).body("Giao dịch bị hủy hoặc thất bại.");
        }

        // Nếu thành công
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
        return ResponseEntity.ok("Thanh toán thành công!");
    }
}
