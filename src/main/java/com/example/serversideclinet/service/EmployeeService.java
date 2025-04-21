package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.EmployeeRequestDTO;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.Role;
import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.repository.AppointmentRepository;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.RoleRepository;
import com.example.serversideclinet.repository.StoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Employee createEmployee(EmployeeRequestDTO dto) {
        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));

        Employee employee = new Employee();
        employee.setEmployeeCode(dto.getEmployeeCode());
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setGender(dto.getGender());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setSpecialization(dto.getSpecialization());
        employee.setStore(store);
        employee.setRoles(roles);
        employee.setAvatarUrl(dto.getAvatarUrl());
        return employeeRepository.save(employee);
    }
}
