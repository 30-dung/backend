package com.example.serversideclinet.service;

import com.example.serversideclinet.config.VnpayConfig;
import com.example.serversideclinet.model.Invoice;
import com.example.serversideclinet.model.InvoiceStatus;
import com.example.serversideclinet.repository.InvoiceRepository;
import com.example.serversideclinet.util.HmacSha512Util;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VnpayService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private EmailService emailService;

    public String createPaymentUrl(Invoice invoice, HttpServletRequest request) {
        // Kiểm tra hóa đơn hợp lệ
        if (invoice == null || invoice.getInvoiceId() == null) {
            throw new IllegalArgumentException("Hóa đơn không hợp lệ");
        }

        if (invoice.getTotalAmount() == null || invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán không hợp lệ");
        }

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VnpayConfig.VNP_TMN_CODE);
        vnp_Params.put("vnp_Amount", String.valueOf(invoice.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue()));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", String.valueOf(invoice.getInvoiceId()));
        vnp_Params.put("vnp_OrderInfo", "Thanh toan hoa don #" + invoice.getInvoiceId());
        vnp_Params.put("vnp_OrderType", "billpayment");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VnpayConfig.VNP_RETURN_URL);
        vnp_Params.put("vnp_IpAddr", getClientIp(request));
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = vnp_Params.get(fieldName);
            if (value != null && !value.isEmpty()) {
                String encodedField = URLEncoder.encode(fieldName, StandardCharsets.UTF_8);
                String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);

                hashData.append(encodedField).append('=').append(encodedValue).append('&');
                query.append(encodedField).append('=').append(encodedValue).append('&');
            }
        }

        String queryUrl = query.substring(0, query.length() - 1);
        String hashDataStr = hashData.substring(0, hashData.length() - 1);
        String secureHash = HmacSha512Util.hmacSHA512(VnpayConfig.VNP_HASH_SECRET, hashDataStr);

        return VnpayConfig.VNP_URL + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;
    }

    @Transactional
    public boolean processPaymentResponse(Map<String, String> params) {
        // Xác thực checksum
        if (!verifyPayment(params)) {
            return false;
        }

        // Kiểm tra trạng thái giao dịch
        String transactionStatus = params.get("vnp_TransactionStatus");
        String invoiceId = params.get("vnp_TxnRef");

        if (!"00".equals(transactionStatus)) {
            return false;
        }

        // Cập nhật hóa đơn thành PAID
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(Long.valueOf(invoiceId));
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            invoice.setTransactionId(params.get("vnp_TransactionNo"));
            invoiceRepository.save(invoice);

            // Gửi email xác nhận thanh toán
            try {
                String emailContent = buildPaymentConfirmationEmail(invoice);
                emailService.sendInvoiceEmail(invoice.getUser().getEmail(),
                        "Xác nhận thanh toán hóa đơn #" + invoice.getInvoiceId(),
                        emailContent);
            } catch (Exception e) {
                // Log error but don't fail the transaction
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }

    private String buildPaymentConfirmationEmail(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>Kính gửi ").append(invoice.getUser().getFullName()).append(",</h3>");
        sb.append("<p>Cảm ơn bạn đã thanh toán hóa đơn tại BarberShop.</p>");
        sb.append("<p><strong>Thông tin thanh toán:</strong></p>");
        sb.append("<ul>");
        sb.append("<li>Mã hóa đơn: #").append(invoice.getInvoiceId()).append("</li>");
        sb.append("<li>Số tiền: ").append(invoice.getTotalAmount()).append(" VND</li>");
        sb.append("<li>Ngày thanh toán: ").append(invoice.getPaidAt()).append("</li>");
        sb.append("<li>Phương thức: VNPAY</li>");
        sb.append("</ul>");
        sb.append("<p>Vui lòng giữ lại thông tin này để đối chiếu khi cần thiết.</p>");

        return sb.toString();
    }

    public boolean verifyPayment(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return false;
        }

        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isEmpty()) {
            return false;
        }

        // Tạo bản sao của params để xử lý mà không ảnh hưởng đến tham số gốc
        Map<String, String> paramsCopy = new HashMap<>(params);
        paramsCopy.remove("vnp_SecureHash");
        paramsCopy.remove("vnp_SecureHashType");

        SortedMap<String, String> sortedParams = new TreeMap<>(paramsCopy);
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                sb.append(encodedKey).append('=').append(encodedValue);
            }
        }

        String calculatedHash = HmacSha512Util.hmacSHA512(VnpayConfig.VNP_HASH_SECRET, sb.toString());

        return receivedHash != null && receivedHash.equalsIgnoreCase(calculatedHash);
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        // Lấy IP đầu tiên nếu có nhiều IP (trường hợp qua proxy)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress != null ? ipAddress : "127.0.0.1";
    }
}