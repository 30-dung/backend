package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.CustomerRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRatingRepository extends JpaRepository<CustomerRating, Integer> {
}
