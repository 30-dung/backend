package com.example.serversideclinet.service;

import com.example.serversideclinet.config.VnpayConfig;
import com.example.serversideclinet.util.HmacSha512Util;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentService {

    public String createPaymentUrl(long amount) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Thanh toan don hang test";
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_IpAddr = "127.0.0.1";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = sdf.format(new Date());
        String vnp_Amount = String.valueOf(amount * 100L);
        String vnp_Locale = "vn";
        String vnp_ReturnUrl = VnpayConfig.VNP_RETURN_URL;
        String vnp_BankCode = "NCB";
        String vnp_OrderType = "billpayment";

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", VnpayConfig.VNP_TMN_CODE);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_BankCode", vnp_BankCode);

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .append("&");
        }

        String queryUrl = query.substring(0, query.length() - 1);
        String vnp_SecureHash = HmacSha512Util.hmacSHA512(VnpayConfig.VNP_HASH_SECRET, queryUrl);
        return VnpayConfig.VNP_URL + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    public String verifyReturn(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");

        Map<String, String> fields = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("vnp_") && !entry.getKey().equals("vnp_SecureHash")) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }

        String query = fields.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .reduce((a, b) -> a + "&" + b).orElse("");

        String signValue = HmacSha512Util.hmacSHA512(VnpayConfig.VNP_HASH_SECRET, query);

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(params.get("vnp_ResponseCode"))) {
                return "success";
            } else {
                return "fail_" + params.get("vnp_ResponseCode");
            }
        } else {
            return "invalid_signature";
        }
    }
}
