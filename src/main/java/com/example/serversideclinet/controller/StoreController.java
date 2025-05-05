package com.example.serversideclinet.controller;

import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    // Add store

    @PostMapping("/add")
    public ResponseEntity<Store> addStore(@RequestBody Store store) {
        Store newStore = storeService.addStore(store);
        return ResponseEntity.ok(newStore);
    }

    // Update store
    @PutMapping("/update/{id}")
    public ResponseEntity<Store> updateStore(@PathVariable Integer id, @RequestBody Store store) {
        Store updatedStore = storeService.updateStore(id, store);
        return ResponseEntity.ok(updatedStore);
    }

    // Delete store
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Integer id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }

    // Locate store by city or district
    @GetMapping("/locate")
    public ResponseEntity<List<Store>> locateStore(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {
        List<Store> stores = storeService.findStoresByCityOrDistrict(city, district);
        return ResponseEntity.ok(stores);
    }
    @GetMapping("/all")
    public ResponseEntity<List<Store>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

}
