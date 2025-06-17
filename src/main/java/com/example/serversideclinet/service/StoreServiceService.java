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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StoreServiceService {

    @Autowired
    private StoreServiceRepository storeServiceRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Transactional
    public StoreService createStoreService(StoreServiceRequest req) {
        Optional<StoreService> existingStoreService = storeServiceRepository.findByStoreStoreIdAndServiceServiceId(req.getStoreId(), req.getServiceId());
        if (existingStoreService.isPresent()) {
            throw new RuntimeException("Dịch vụ này đã tồn tại cho cửa hàng này. Vui lòng cập nhật thay vì tạo mới.");
        }

        Store store = storeRepository.findById(req.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + req.getStoreId()));
        ServiceEntity service = serviceRepository.findById(req.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + req.getServiceId()));

        StoreService ss = new StoreService();
        ss.setStore(store);
        ss.setService(service);
        ss.setPrice(req.getPrice());

        return storeServiceRepository.save(ss);
    }

    @Transactional
    public List<StoreService> getServicesByStoreId(Integer storeId) {
        return storeServiceRepository.findByStoreStoreId(storeId);
    }


    @Transactional
    public List<StoreService> getAllStoreServices() {
        return storeServiceRepository.findAll();
    }


    @Transactional
    public StoreService updateStoreService(Integer id, StoreServiceRequest req) {
        return storeServiceRepository.findById(id).map(existingStoreService -> {
            Store store = storeRepository.findById(req.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Store not found with ID: " + req.getStoreId()));
            ServiceEntity service = serviceRepository.findById(req.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Service not found with ID: " + req.getServiceId()));

            existingStoreService.setStore(store);
            existingStoreService.setService(service);
            existingStoreService.setPrice(req.getPrice());
            return storeServiceRepository.save(existingStoreService);
        }).orElse(null);
    }


    @Transactional
    public boolean deleteStoreService(Integer id) {
        if (storeServiceRepository.existsById(id)) {
            storeServiceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}