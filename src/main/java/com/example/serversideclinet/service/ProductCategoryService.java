package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.ProductCategoryDTO;
import com.example.serversideclinet.dto.ProductDTO;
import com.example.serversideclinet.model.ProductCategory;
import com.example.serversideclinet.repository.ProductCategoryRepository;
import com.example.serversideclinet.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCategoryService {
    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    public List<ProductCategoryDTO> getAllCategories(){
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductCategoryDTO getCategoryById(Long id, Pageable pageable){
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        ProductCategoryDTO dto = convertToDTO(category);
        Page<ProductDTO> products = productRepository.findByCategory(category, pageable)
                .map(productService::convertToDTO);
        dto.setProducts(products);
        return dto;
    }

    public ProductCategoryDTO createCategory(ProductCategoryDTO categoryDTO){
        ProductCategory category = new ProductCategory();
        category.setName(categoryDTO.getName());
        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    public ProductCategoryDTO updateCategory(Long id, ProductCategoryDTO categoryDTO){
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(categoryDTO.getName());
        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    public void deleteCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepository.delete(category);
    }

    private ProductCategoryDTO convertToDTO(ProductCategory category){
        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
