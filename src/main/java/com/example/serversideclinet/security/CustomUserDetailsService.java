package com.example.serversideclinet.security;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.User;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private EmployeeRepository employeeRepository;
//
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        // Tìm trong bảng User
//        Optional<User> userOptional = userRepository.findByEmail(email);
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//            System.out.println("User found: " + user.getEmail());
//            System.out.println("User roles: " + user.getRoles());
//
//            return new org.springframework.security.core.userdetails.User(
//                    user.getEmail(),
//                    user.getPassword(),
//                    user.getRoles().stream()
//                            .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
//                            .collect(Collectors.toList())
//            );
//        }
//
//        // Nếu không thấy trong User, tìm trong Employee
//        return employeeRepository.findByEmail(email)
//                .map(employee -> {
//                    System.out.println("Employee found: " + employee.getEmail());
//                    System.out.println("Employee roles: " + employee.getRoles());
//
//                    return new org.springframework.security.core.userdetails.User(
//                            employee.getEmail(),
//                            employee.getPassword(),
//                            employee.getRoles().stream()
//                                    .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
//                                    .collect(Collectors.toList())
//                    );
//                })
//                .orElseThrow(() -> new UsernameNotFoundException("No user or employee found with email: " + email));
//    }
//}

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tìm trong bảng User
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user != null) {
            System.out.println("User found: " + user.getEmail());
            System.out.println("User roles: " + user.getRoles());
            return new CustomUserDetails(user);
        }

        // Tìm trong bảng Employee
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user or employee found with email: " + email));
        System.out.println("Employee found: " + employee.getEmail());
        System.out.println("Employee roles: " + employee.getRoles());
        return new CustomUserDetails(employee);
    }
}
