package com.example.serversideclinet.controller;

import com.example.serversideclinet.dto.CityWithCountDTO;
import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequestMapping("/api/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @PostMapping("/add")
    public ResponseEntity<Store> addStore(@RequestBody Store store) {
        Store newStore = storeService.addStore(store);
        return ResponseEntity.ok(newStore);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Store> updateStore(@PathVariable Integer id, @RequestBody Store store) {
        Store updatedStore = storeService.updateStore(id, store);
        return ResponseEntity.ok(updatedStore);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Integer id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/locate")
    public ResponseEntity<List<Store>> locateStore(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {
        if (district != null && !district.trim().isEmpty() && (city == null || city.trim().isEmpty())) {
            return ResponseEntity.badRequest().body(null); // District requires city
        }
        List<Store> stores = storeService.findStoresByCityOrDistrict(city, district);
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Store>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @GetMapping("/cities")
    public ResponseEntity<List<CityWithCountDTO>> getCities() {
        List<CityWithCountDTO> cities = storeService.getCitiesWithStoreCount();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/districts")
    public ResponseEntity<List<CityWithCountDTO>> getDistrictsByCity(@RequestParam String cityProvince) {
        List<CityWithCountDTO> districts = storeService.getDistrictsWithStoreCountByCity(cityProvince);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Integer id) {
        Store store = storeService.getStoreById(id);
        if (store == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(store);
    }
}