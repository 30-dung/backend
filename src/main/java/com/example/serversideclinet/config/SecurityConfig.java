package com.example.serversideclinet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF (nên để true nếu dùng form)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Cho phép tất cả request
                )
                .formLogin(form -> form.disable()) // Tắt form login mặc định
                .httpBasic(basic -> basic.disable()); // Tắt basic auth luôn (tuỳ chọn)

        return http.build();
    }
}
