package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.InvoiceDTO;
import com.example.serversideclinet.model.Invoice;
import com.example.serversideclinet.model.InvoiceStatus;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    // Phương thức lấy danh sách hóa đơn của khách hàng theo trạng thái
    public List<InvoiceDTO> findByStatus(User user, InvoiceStatus status) {
        List<Invoice> invoices = invoiceRepository.findByUserAndStatus(user, status);
        return convertInvoicesToDTOs(invoices);
    }

    // Phương thức lấy tất cả hóa đơn của khách hàng
    public List<InvoiceDTO> findAllByUser(User user) {
        List<Invoice> invoices = invoiceRepository.findByUser(user);
        return convertInvoicesToDTOs(invoices);
    }

    // Phương thức chuyển đổi từ Invoice sang InvoiceDTO
    private List<InvoiceDTO> convertInvoicesToDTOs(List<Invoice> invoices) {
        return invoices.stream()
                .map(invoice -> new InvoiceDTO(
                        invoice.getInvoiceId(),
                        invoice.getUser().getEmail(),  // Lấy email của user
                        invoice.getTotalAmount(),
                        invoice.getStatus(),
                        invoice.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
