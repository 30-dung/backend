package com.example.serversideclinet.dto;

import java.time.LocalDateTime;

public class CustomerRatingDTO {

    private Integer ratingId;
    private Integer stars;
    private String comment;
    private LocalDateTime ratingDate;

    public CustomerRatingDTO(Integer ratingId, Integer stars, String comment, LocalDateTime ratingDate) {
        this.ratingId = ratingId;
        this.stars = stars;
        this.comment = comment;
        this.ratingDate = ratingDate;
    }

    public Integer getRatingId() {
        return ratingId;
    }

    public void setRatingId(Integer ratingId) {
        this.ratingId = ratingId;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getRatingDate() {
        return ratingDate;
    }

    public void setRatingDate(LocalDateTime ratingDate) {
        this.ratingDate = ratingDate;
    }
}
