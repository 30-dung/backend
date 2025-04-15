package com.example.serversideclinet.model;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
// Role.java
@Entity
@Table(name = "Role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    @Column(unique = true, nullable = false)
    private String roleName;

    private String description;

    // Getters and Setters
}




