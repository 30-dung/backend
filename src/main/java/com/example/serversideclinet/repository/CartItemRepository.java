package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
