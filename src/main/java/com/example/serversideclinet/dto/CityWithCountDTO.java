package com.example.serversideclinet.dto;

public class CityWithCountDTO {
    private String cityProvince;
    private Long storeCount;

    public CityWithCountDTO(String cityProvince, Long storeCount) {
        this.cityProvince = cityProvince;
        this.storeCount = storeCount;
    }

    // Getters and setters
    public String getCityProvince() {
        return cityProvince;
    }

    public void setCityProvince(String cityProvince) {
        this.cityProvince = cityProvince;
    }

    public Long getStoreCount() {
        return storeCount;
    }

    public void setStoreCount(Long storeCount) {
        this.storeCount = storeCount;
    }
}