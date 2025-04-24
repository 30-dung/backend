package com.example.serversideclinet.security;

import lombok.Getter;

@Getter
public class CustomPrincipal {
    private final String email;
    private final Integer userId;

    public CustomPrincipal(String email, Integer userId) {
        this.email = email;
        this.userId = userId;
    }
}
