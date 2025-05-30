package com.example.serversideclinet.config;

import com.example.serversideclinet.security.CustomUserDetailsService;
import com.example.serversideclinet.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter; // Đảm bảo import này có

import java.util.Arrays; // Thêm import này

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Đảm bảo gọi cấu hình CORS từ bean corsConfigurationSource()
                .csrf(csrf -> csrf.disable()) // Tắt CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/register",
                                "/error"
                        ).permitAll()// Cho phép truy cập không cần auth
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/store/**").permitAll() // Cho phép truy cập không cần auth
                        .requestMatchers("/api/reviews/**").permitAll() // Cho phép truy cập không cần auth
                        .requestMatchers("/api/appointments/**").hasRole("CUSTOMER")
                        .requestMatchers("/payment/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Thêm vào danh sách permitAll
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated() // Các request khác cần auth
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Không dùng session
                );

        // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Đổi tên từ corsFilter() thành corsConfigurationSource() để tích hợp tốt hơn với http.cors()
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        System.out.println("Configuring CORS Filter...");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        // THAY ĐỔI QUAN TRỌNG NHẤT Ở ĐÂY: Cho phép tất cả các origins
        config.setAllowedOriginPatterns(Arrays.asList("*")); // Sử dụng setAllowedOriginPatterns với "*"
        // Hoặc nếu bạn muốn cụ thể hơn cho localhost động:
        // config.setAllowedOrigins(Arrays.asList("http://localhost:*", "http://10.0.2.2:9090", "http://192.168.1.32:9090"));

        config.addAllowedHeader("*"); // Cho phép tất cả các header
        config.addAllowedMethod("*"); // Cho phép tất cả các phương thức HTTP (GET, POST, PUT, DELETE, OPTIONS, v.v.)
        config.setMaxAge(3600L); // Thời gian cache preflight request (tuỳ chọn)

        source.registerCorsConfiguration("/**", config); // Áp dụng cấu hình cho tất cả các path
        return source;
    }
}