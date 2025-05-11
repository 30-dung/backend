package com.example.serversideclinet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {
    @JsonProperty("token")
    private String token;

    @JsonProperty("role")
    private String role;

    // Constructor
    public AuthResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

    // Getter và Setter
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // toString để debug
    @Override
    public String toString() {
        return "AuthResponse{token='" + token + "', role='" + role + "'}";
    }
}
