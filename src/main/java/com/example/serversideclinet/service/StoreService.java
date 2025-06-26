// service/StoreService.java
package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.CityWithCountDTO;
import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException; // Import IOException
import java.nio.file.Files; // Import Files
import java.nio.file.Path; // Import Path
import java.nio.file.Paths; // Import Paths
import java.util.List;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Value("${file.upload-dir}") // Đảm bảo cấu hình thư mục upload
    private String uploadDir;

    // Helper method to delete old image file
    private void deleteOldImageFile(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fileName = imageUrl.replace("/images/", ""); // Loại bỏ tiền tố
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            try {
                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                    Files.delete(filePath);
                    System.out.println("Deleted old store image: " + filePath.toString());
                }
            } catch (IOException e) {
                System.err.println("Could not delete old store image " + filePath.toString() + ": " + e.getMessage());
            }
        }
    }

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
            // Xóa ảnh cũ nếu URL ảnh thay đổi hoặc bị xóa
            if (store.getStoreImages() != null && !store.getStoreImages().isEmpty() &&
                    (updatedStore.getStoreImages() == null || updatedStore.getStoreImages().isEmpty() || // Nếu ảnh mới là rỗng
                            !store.getStoreImages().equals(updatedStore.getStoreImages()))) { // Hoặc ảnh mới khác ảnh cũ
                deleteOldImageFile(store.getStoreImages());
            }

            store.setStoreName(updatedStore.getStoreName());
            store.setPhoneNumber(updatedStore.getPhoneNumber());
            store.setCityProvince(updatedStore.getCityProvince());
            store.setStoreImages(updatedStore.getStoreImages()); // Cập nhật URL ảnh mới (hoặc rỗng)
            store.setDistrict(updatedStore.getDistrict());
            store.setOpeningTime(updatedStore.getOpeningTime());
            store.setClosingTime(updatedStore.getClosingTime());
            store.setDescription(updatedStore.getDescription());
            // store.setStoreImages(updatedStore.getStoreImages()); // Dòng này bị lặp lại, có thể xóa
            store.setAverageRating(updatedStore.getAverageRating());
            return storeRepository.save(store);
        }).orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + id));
    }

    public List<Store> findStoresByCityOrDistrict(String city, String district) {
        String cityProvince = (city == null || city.trim().isEmpty()) ?
                null : city;
        String districtParam = (district == null || district.trim().isEmpty()) ? null : district;
        return storeRepository.findByCityProvinceAndDistrict(cityProvince, districtParam);
    }

    public void deleteStore(int id) {
        Store existingStore = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found bacterwith id: " + id));
        // Xóa ảnh cửa hàng liên quan trước khi xóa entity
        deleteOldImageFile(existingStore.getStoreImages());
        storeRepository.delete(existingStore);
    }

    public List<CityWithCountDTO> getCitiesWithStoreCount() {
        return storeRepository.findCitiesWithStoreCount();
    }

    public List<CityWithCountDTO> getDistrictsWithStoreCountByCity(String cityProvince) {
        return storeRepository.findDistrictsWithStoreCountByCity(cityProvince);
    }
}