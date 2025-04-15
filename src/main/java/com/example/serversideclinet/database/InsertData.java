package com.example.serversideclinet.database;

import com.example.serversideclinet.repository.InvoiceDetailRepository;
import com.example.serversideclinet.repository.InvoiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InsertData {
    @Bean
    public CommandLineRunner loadData(
                                       ) {
        return args -> {
        };
    }
}
