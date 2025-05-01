package com.example.serversideclinet.repository;

import com.example.serversideclinet.model.Invoice;
import com.example.serversideclinet.model.InvoiceStatus;
import com.example.serversideclinet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Optional<Invoice> findFirstByUserAndStatus(User user, InvoiceStatus status);
    List<Invoice> findByUserAndStatus(User user, InvoiceStatus invoiceStatus);
}
