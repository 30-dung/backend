package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.ProductDTO;
import com.example.serversideclinet.dto.ProductImageDTO;
import com.example.serversideclinet.model.Product;
import com.example.serversideclinet.model.ProductCategory;
import com.example.serversideclinet.model.ProductImage;
import com.example.serversideclinet.repository.ProductCategoryRepository;
import com.example.serversideclinet.repository.ProductImageRepository;
import com.example.serversideclinet.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDTO(product);
    }

    public ProductDTO addProduct(ProductDTO productDTO){
        Product product = new Product();
        updateProductFromDTO(product, productDTO);
        product = productRepository.save(product);
        return convertToDTO(product);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        updateProductFromDTO(product, productDTO);
        product = productRepository.save(product);
        return convertToDTO(product);
    }

    public void deleteProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }

    public ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setImages(product.getImages().stream().map(img -> {
            ProductImageDTO imgDTO = new ProductImageDTO();
            imgDTO.setImageId(img.getImageId());
            imgDTO.setImageUrl(img.getImageUrl());
            imgDTO.setIsPrimary(img.getIsPrimary());
            return imgDTO;
        }).collect(Collectors.toList()));
        return dto;
    }

    private void updateProductFromDTO(Product product, ProductDTO dto){
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        if(dto.getCategoryId() != null){
            ProductCategory productCategory = productCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(productCategory);
        }else {
            product.setCategory(null);
        }

        product.getImages().clear();
        if(dto.getImages() != null){
            for(ProductImageDTO imageDTO : dto.getImages()){
                ProductImage image = new ProductImage();
                image.setImageUrl(imageDTO.getImageUrl());
                image.setIsPrimary(imageDTO.getIsPrimary() != null ? imageDTO.getIsPrimary()
                        : false);
                image.setProduct(product);
                product.getImages().add(image);
            }
        }
    }
}
