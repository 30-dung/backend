package com.example.serversideclinet.security;

import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

//public class CustomUserDetails implements UserDetails {
//    private String email;
//    private String password;
//    private Collection<? extends GrantedAuthority> authorities;
//    private boolean isEmployee;
//
//    public CustomUserDetails(User user) {
//        this.email = user.getEmail();
//        this.password = user.getPassword();
//        this.authorities = user.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
//                .collect(Collectors.toList());
//        this.isEmployee = false;
//    }
//
//    public CustomUserDetails(Employee employee) {
//        this.email = employee.getEmail();
//        this.password = employee.getPassword();
//        this.authorities = employee.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
//                .collect(Collectors.toList());
//        this.isEmployee = true;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return authorities;
//    }
//
//    @Override
//    public String getPassword() {
//        return password;
//    }
//
//    @Override
//    public String getUsername() {
//        return email;
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//
//    public boolean isEmployee() {
//        return isEmployee;
//    }
//}



public class CustomUserDetails implements UserDetails {
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean isEmployee;
    private Integer userId;

    public CustomUserDetails(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());
        this.isEmployee = false;
        this.userId = user.getUserId();
    }

    public CustomUserDetails(Employee employee) {
        this.email = employee.getEmail();
        this.password = employee.getPassword();
        this.authorities = employee.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());
        this.isEmployee = true;
        this.userId = employee.getEmployeeId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isEmployee() {
        return isEmployee;
    }

    public Integer getUserId() {
        return userId;
    }
}
