package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Cart;
import com.example.serversideclinet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
