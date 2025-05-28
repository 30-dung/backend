package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.StoreService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreServiceRepository extends JpaRepository<StoreService, Integer> {

    List<StoreService> findByStoreStoreId(Integer storeId);
//    @Override
//    Optional<StoreService> findById(Integer integer);
}
