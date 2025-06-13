// InvoiceService.java
package com.example.serversideclinet.service;

import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    public List<Invoice> findByStatus(User user, InvoiceStatus status) {
        return invoiceRepository.findByUserAndStatus(user, status);
    }

    public List<Invoice> findAllByUser(User user) {
        return invoiceRepository.findByUser(user);
    }

}

