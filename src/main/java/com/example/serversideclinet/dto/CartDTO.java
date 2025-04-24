package com.example.serversideclinet.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartDTO {
    private Long id;
    private Integer userId;
    private List<CartItemDTO> items;
    private double totalPrice;
}
