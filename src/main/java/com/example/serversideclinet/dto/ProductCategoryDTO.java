package com.example.serversideclinet.dto;

import jakarta.validation.constraints.NotBlank;

import org.springframework.data.domain.Page;


public class ProductCategoryDTO {
    private Long id;
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
    private Page<ProductDTO> products;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Page<ProductDTO> getProducts() {
        return products;
    }

    public void setProducts(Page<ProductDTO> products) {
        this.products = products;
    }
}
