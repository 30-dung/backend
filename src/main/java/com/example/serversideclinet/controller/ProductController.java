package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.ProductDTO;
import com.example.serversideclinet.service.ProductService;
import jakarta.validation.Valid;
import org.springdoc.core.configuration.SpringDocUIConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> addProduct(@Valid  @RequestBody ProductDTO productDTO){
        return ResponseEntity.ok(productService.addProduct(productDTO));
    }

    @PutMapping
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,@Valid @RequestBody ProductDTO productDTO ){
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
