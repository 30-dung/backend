package com.example.serversideclinet.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
// package com.example.serversideclinet.dto;
public class StoreServiceRequest {
    private Integer storeId;
    private Integer serviceId;
    private BigDecimal price;

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
