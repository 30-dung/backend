package com.example.serversideclinet.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDTO {
    private Long id;
    private Integer userId;
    private double totalPrice;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
}
