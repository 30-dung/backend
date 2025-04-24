package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
