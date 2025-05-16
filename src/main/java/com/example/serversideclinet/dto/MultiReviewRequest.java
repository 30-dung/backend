package com.example.serversideclinet.dto;

public class MultiReviewRequest {
    private Integer storeId;
    private Integer storeRating;

    private Integer serviceId;
    private Integer serviceRating;

    private Integer employeeId;
    private Integer employeeRating;

    private String comment; // Dùng chung cho tất cả

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Integer getStoreRating() {
        return storeRating;
    }

    public void setStoreRating(Integer storeRating) {
        this.storeRating = storeRating;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getServiceRating() {
        return serviceRating;
    }

    public void setServiceRating(Integer serviceRating) {
        this.serviceRating = serviceRating;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getEmployeeRating() {
        return employeeRating;
    }

    public void setEmployeeRating(Integer employeeRating) {
        this.employeeRating = employeeRating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
