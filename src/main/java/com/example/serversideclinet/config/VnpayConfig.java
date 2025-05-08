package com.example.serversideclinet.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class VnpayConfig {
    public static final String VNP_TMN_CODE = "0GAZQ65O";
    public static final String VNP_HASH_SECRET = "QAM8J01M408VIEMXRYPL7JPTW8O3F4SP";
    public static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String VNP_RETURN_URL = "http://localhost:8080/payment/return"; // URL xử lý kết quả
}