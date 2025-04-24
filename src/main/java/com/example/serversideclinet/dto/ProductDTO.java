package com.example.serversideclinet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductDTO {
    private Long id;
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;
    private String description;
    @NotNull(message = "Giá sản phẩm không được để trống")
    @Positive(message = "Giá sản phẩm phải lớn hơn 0")
    private double price;
    private Long categoryId;
    private List<ProductImageDTO> images;
}
