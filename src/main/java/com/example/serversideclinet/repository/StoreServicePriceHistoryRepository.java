package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.StoreServicePriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreServicePriceHistoryRepository extends JpaRepository<StoreServicePriceHistory, Integer> {
}
