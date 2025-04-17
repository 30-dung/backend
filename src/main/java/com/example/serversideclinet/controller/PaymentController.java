package com.example.serversideclinet.controller;

import com.example.serversideclinet.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;


    @PostMapping("/payment/create")
    public String createPayment(@RequestParam("amount") long amount, RedirectAttributes redirectAttributes) {
        try {
            String paymentUrl = paymentService.createPaymentUrl(amount);
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi khi tạo URL thanh toán: " + e.getMessage());
            return "redirect:/";
        }
    }
    @GetMapping("/payment/return")
    public String paymentReturn(@RequestParam Map<String, String> params, Model model) {
        String result = paymentService.verifyReturn(params);
        switch (result) {
            case "success":
                model.addAttribute("message", "Giao dịch thành công!");
                model.addAttribute("txnRef", params.get("vnp_TxnRef"));
                model.addAttribute("amount", Long.parseLong(params.get("vnp_Amount")) / 100);
                break;
            case "invalid_signature":
                model.addAttribute("message", "Chữ ký không hợp lệ!");
                break;
            default:
                if (result.startsWith("fail_")) {
                    model.addAttribute("message", "Giao dịch thất bại! Mã lỗi: " + result.split("_")[1]);
                }
        }
        return "payment";
    }
}
