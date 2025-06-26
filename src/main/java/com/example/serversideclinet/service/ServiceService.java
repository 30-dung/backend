// service/ServiceService.java
package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.ServiceRequest;
import com.example.serversideclinet.model.ServiceEntity;
import com.example.serversideclinet.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException; // Import IOException
import java.nio.file.Files; // Import Files
import java.nio.file.Path; // Import Path
import java.nio.file.Paths; // Import Paths
import java.util.List;
import java.util.Optional;

@Service
public class ServiceService {
    @Autowired
    private ServiceRepository serviceRepository;

    @Value("${file.upload-dir}") // Đảm bảo cấu hình thư mục upload
    private String uploadDir;

    // Helper method to delete old image file
    private void deleteOldImageFile(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Loại bỏ tiền tố "/images/" để lấy đường dẫn tương đối của file
            String fileName = imageUrl.replace("/images/", "");
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            try {
                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                    Files.delete(filePath);
                    System.out.println("Deleted old service image: " + filePath.toString());
                }
            } catch (IOException e) {
                System.err.println("Could not delete old service image " + filePath.toString() + ": " + e.getMessage());
                // Log the error but don't throw, as the main operation (update/delete entity) should still proceed.
            }
        }
    }

    @Transactional
    public ServiceEntity create(ServiceRequest request){
        ServiceEntity service = new ServiceEntity();
        service.setServiceName(request.getServiceName());
        service.setDescription(request.getDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setServiceImg(request.getServiceImg()); // serviceImg là URL đã được upload
        return serviceRepository.save(service);
    }
    @Transactional
    public List<ServiceEntity> getAll() {
        return serviceRepository.findAll();
    }

    @Transactional
    public Optional<ServiceEntity> getById(Integer id) {
        return serviceRepository.findById(id);
    }

    @Transactional
    public Optional<ServiceEntity> update(Integer id, ServiceRequest request) {
        return serviceRepository.findById(id).map(existing -> {
            // Xóa ảnh cũ nếu URL ảnh thay đổi hoặc bị xóa
            // So sánh đường dẫn tương đối (existing.getServiceImg()) với đường dẫn tương đối mới (request.getServiceImg())
            if (existing.getServiceImg() != null && !existing.getServiceImg().isEmpty() &&
                    (request.getServiceImg() == null || request.getServiceImg().isEmpty() || // Nếu ảnh mới là rỗng (người dùng muốn xóa)
                            !existing.getServiceImg().equals(request.getServiceImg()))) { // Hoặc ảnh mới khác ảnh cũ
                deleteOldImageFile(existing.getServiceImg());
            }

            existing.setServiceName(request.getServiceName());
            existing.setDescription(request.getDescription());
            existing.setDurationMinutes(request.getDurationMinutes());
            existing.setServiceImg(request.getServiceImg()); // Cập nhật URL ảnh mới (hoặc rỗng)
            return serviceRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Integer id) {
        Optional<ServiceEntity> serviceOptional = serviceRepository.findById(id);
        if (serviceOptional.isPresent()) {
            ServiceEntity service = serviceOptional.get();
            // Xóa ảnh dịch vụ liên quan trước khi xóa entity
            deleteOldImageFile(service.getServiceImg());
            serviceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}