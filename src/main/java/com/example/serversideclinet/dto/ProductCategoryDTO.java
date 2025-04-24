package com.example.serversideclinet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class ProductCategoryDTO {
    private Long id;
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
    private Page<ProductDTO> products;
}
