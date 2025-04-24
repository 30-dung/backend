package com.example.serversideclinet.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private double price;
    private int quantity;
}
