package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Product;
import com.example.serversideclinet.model.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    long countByCategory(ProductCategory category);
    List<Product> findByCategory(ProductCategory category);
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);
}
