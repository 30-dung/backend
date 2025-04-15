package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Integer> {
}
