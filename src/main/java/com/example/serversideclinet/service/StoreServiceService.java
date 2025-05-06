package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.StoreServiceRequest;
import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.model.ServiceEntity;
import com.example.serversideclinet.model.StoreService;
import com.example.serversideclinet.repository.StoreServiceRepository;
import com.example.serversideclinet.repository.StoreRepository;
import com.example.serversideclinet.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceService {

    @Autowired
    private StoreServiceRepository storeServiceRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ServiceRepository serviceRepository;


    @Autowired
    private StoreServicePriceHistoryRepository StoreServicePriceHistoryRepository;

    public StoreService createStoreService(StoreServiceRequest req) {
        // Từ đây req.getStoreId(), getServiceId(), getPrice() sẽ tìm được
        Store store = storeRepository.findById(req.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));
        ServiceEntity service = serviceRepository.findById(req.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        StoreService ss = new StoreService();
        ss.setStore(store);         // phải import com.example.serversideclinet.model.StoreService
        ss.setService(service);
        ss.setPrice(req.getPrice());

        return storeServiceRepository.save(ss);
    }


}
