package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.ProductCategoryDTO;
import com.example.serversideclinet.service.ProductCategoryService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class ProductCategoryController {
    @Autowired
    private ProductCategoryService productCategoryService;

    @GetMapping
    public ResponseEntity<List<ProductCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(productCategoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategoryDTO> getCategoryById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productCategoryService.getCategoryById(id, pageable));
    }

    @PostMapping
    public ResponseEntity<ProductCategoryDTO> addCategory(@Valid @RequestBody ProductCategoryDTO productCategoryDTO){
        return ResponseEntity.ok(productCategoryService.createCategory(productCategoryDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCategoryDTO> updateCategory( @PathVariable Long id,@Valid @RequestBody ProductCategoryDTO productCategoryDTO){
        return ResponseEntity.ok(productCategoryService.updateCategory(id, productCategoryDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        productCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
