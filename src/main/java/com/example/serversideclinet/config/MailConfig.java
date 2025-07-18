package com.example.serversideclinet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private String auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private String starttls;

    @Bean
    public JavaMailSender getJavaMailSender() {
        logger.info("Configuring JavaMailSender with host: {}, port: {}, username: {}", host, port, username);
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        if (username == null || username.trim().isEmpty()) {
            logger.error("Email username is not configured or is empty");
            throw new IllegalStateException("Email username must be configured in application.properties");
        }
        if (password == null || password.trim().isEmpty()) {
            logger.error("Email password is not configured or is empty");
            throw new IllegalStateException("Email password must be configured in application.properties");
        }
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttls);
        props.put("mail.debug", "true");

        logger.info("JavaMailSender bean created successfully");
        return mailSender;
    }
}