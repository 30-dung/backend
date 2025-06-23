package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.StoreService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreServiceRepository extends JpaRepository<StoreService, Integer> {

    List<StoreService> findByStoreStoreId(Integer storeId);
    // Phương thức hiện có: Tìm StoreService dựa trên storeId và serviceId để kiểm tra trùng lặp
    Optional<StoreService> findByStoreStoreIdAndServiceServiceId(Integer storeId, Integer serviceId);
}