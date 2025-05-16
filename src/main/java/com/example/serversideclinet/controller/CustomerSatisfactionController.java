package com.example.serversideclinet.controller;


import com.example.serversideclinet.model.CustomerSatisfaction;
import com.example.serversideclinet.service.CustomerSatisfactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer-satisfaction")
public class CustomerSatisfactionController {

    @Autowired
    private CustomerSatisfactionService customerSatisfactionService;

    /**
     * API để gửi đánh giá của khách hàng
     *
     * @param customerSatisfaction đánh giá từ phía client
     * @return đánh giá đã lưu
     */
    @PostMapping
    public ResponseEntity<?> createCustomerSatisfaction(@RequestBody CustomerSatisfaction customerSatisfaction) {
        try {
            CustomerSatisfaction saved = customerSatisfactionService.saveCustomerSatisfaction(customerSatisfaction);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lưu đánh giá: " + e.getMessage());
        }
    }
}
