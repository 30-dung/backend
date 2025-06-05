package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.ServiceRequest;
import com.example.serversideclinet.model.ServiceEntity;
import com.example.serversideclinet.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceService {
    @Autowired
    private ServiceRepository serviceRepository;

    @Transactional
    public ServiceEntity create(ServiceRequest request){
        ServiceEntity service = new ServiceEntity();
        service.setServiceName(request.getServiceName());
        service.setDescription(request.getDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setServiceImg(request.getServiceImg());
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
            existing.setServiceName(request.getServiceName());
            existing.setDescription(request.getDescription());
            existing.setDurationMinutes(request.getDurationMinutes());
            return serviceRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Integer id) {
        if (serviceRepository.existsById(id)) {
            serviceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
