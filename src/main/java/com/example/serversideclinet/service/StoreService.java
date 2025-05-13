package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.CityWithCountDTO;
import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    public Store getStoreById(int id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + id));
    }

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    public Store addStore(Store store) {
        return storeRepository.save(store);
    }

    public Store updateStore(Integer id, Store updatedStore) {
        return storeRepository.findById(id).map(store -> {
            store.setStoreName(updatedStore.getStoreName());
            store.setPhoneNumber(updatedStore.getPhoneNumber());
            store.setCityProvince(updatedStore.getCityProvince());
            store.setDistrict(updatedStore.getDistrict());
            store.setOpeningTime(updatedStore.getOpeningTime());
            store.setClosingTime(updatedStore.getClosingTime());
            store.setDescription(updatedStore.getDescription());
            store.setAverageRating(updatedStore.getAverageRating());
            return storeRepository.save(store);
        }).orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + id));
    }

    public List<Store> findStoresByCityOrDistrict(String city, String district) {
        String cityProvince = (city == null || city.trim().isEmpty()) ? null : city;
        String districtParam = (district == null || district.trim().isEmpty()) ? null : district;
        return storeRepository.findByCityProvinceAndDistrict(cityProvince, districtParam);
    }

    public void deleteStore(int id) {
        Store existingStore = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found bacterwith id: " + id));
        storeRepository.delete(existingStore);
    }

    public List<CityWithCountDTO> getCitiesWithStoreCount() {
        return storeRepository.findCitiesWithStoreCount();
    }

    public List<CityWithCountDTO> getDistrictsWithStoreCountByCity(String cityProvince) {
        return storeRepository.findDistrictsWithStoreCountByCity(cityProvince);
    }
}