package com.example.serversideclinet.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageDTO {
    private Long imageId;
    private String imageUrl;
    private Boolean isPrimary;
}
