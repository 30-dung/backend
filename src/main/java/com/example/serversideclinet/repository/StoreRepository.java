package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Integer> {
    List<Store> findByCityProvinceOrDistrict(String cityProvince, String district);
}
