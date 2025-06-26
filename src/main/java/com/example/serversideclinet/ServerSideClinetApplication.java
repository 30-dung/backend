package com.example.serversideclinet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableJpaRepositories(basePackages = "com.example.serversideclinet.repository")
@EntityScan(basePackages = "com.example.serversideclinet.model")
@EnableAsync
public class ServerSideClinetApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerSideClinetApplication.class, args);
    }

}
