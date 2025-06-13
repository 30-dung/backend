package com.example.serversideclinet.dto;


public class MultiReviewRequestDTO {
    private Integer invoiceId;
    private int storeRating;
    private int serviceRating;
    private int employeeRating;
    private String comment;

    // Getters & setters
    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getStoreRating() {
        return storeRating;
    }

    public void setStoreRating(int storeRating) {
        this.storeRating = storeRating;
    }

    public int getServiceRating() {
        return serviceRating;
    }

    public void setServiceRating(int serviceRating) {
        this.serviceRating = serviceRating;
    }

    public int getEmployeeRating() {
        return employeeRating;
    }

    public void setEmployeeRating(int employeeRating) {
        this.employeeRating = employeeRating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
