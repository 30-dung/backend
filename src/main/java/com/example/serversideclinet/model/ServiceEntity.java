package com.example.serversideclinet.model;
import jakarta.persistence.*;

// Service.java
@Entity
@Table(name = "Service")
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceId;

    @Column(nullable = false)
    private String serviceName;

    private String description;

    @Column(nullable = false)
    private Short durationMinutes;

    private String serviceImg;



// Getters and Setters
    public String getServiceImg() {
         return serviceImg;
    }
    public void setServiceImg(String serviceImg) {
        this.serviceImg = serviceImg;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Short getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Short durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}