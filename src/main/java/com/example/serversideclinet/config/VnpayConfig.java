package com.example.serversideclinet.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class VnpayConfig {
    // Lấy thông tin từ VNPAY email
    public static final String VNP_RETURN_URL = "http://localhost:9090/api/payment/return"; // ✅ CHÍNH XÁC
    public static final String VNP_TMN_CODE = "0GAZQ65O"; // Mã Website
    public static final String VNP_HASH_SECRET = "QAM8J01M408VIEMXRYPL7JPTW8O3F4SP"; // Secret Key
    public static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"; // URL thanh toán
}
