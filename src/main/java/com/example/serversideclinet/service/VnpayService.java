package com.example.serversideclinet.service;

import com.example.serversideclinet.config.VnpayConfig;
import com.example.serversideclinet.model.Invoice;
import com.example.serversideclinet.util.HmacSha512Util;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VnpayService {

    public String createPaymentUrl(Invoice invoice, HttpServletRequest request) {
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
        vnp_Params.put("vnp_IpAddr", request.getRemoteAddr());
        vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = vnp_Params.get(fieldName);
            String encodedField = URLEncoder.encode(fieldName, StandardCharsets.UTF_8);
            String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);

            hashData.append(encodedField).append('=').append(encodedValue).append('&');
            query.append(encodedField).append('=').append(encodedValue).append('&');
        }

        String queryUrl = query.substring(0, query.length() - 1);
        String hashDataStr = hashData.substring(0, hashData.length() - 1);
        String secureHash = HmacSha512Util.hmacSHA512(VnpayConfig.VNP_HASH_SECRET, hashDataStr);

        return VnpayConfig.VNP_URL + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;
    }

    public boolean verifyPayment(Map<String, String> params) {
        String receivedHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        SortedMap<String, String> sortedParams = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            sb.append(encodedKey).append('=').append(encodedValue);
        }

        String calculatedHash = HmacSha512Util.hmacSHA512(VnpayConfig.VNP_HASH_SECRET, sb.toString());

        return receivedHash != null && receivedHash.equalsIgnoreCase(calculatedHash);
    }
}
