// src/main/java/com/example/serversideclinet/service/UserService.java
package com.example.serversideclinet.service;

import com.example.serversideclinet.model.User;
import com.example.serversideclinet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null); // tra ve null neu ko tim dc
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // New method for admin to get user by ID
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    // New method for admin to delete user by ID
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}