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
import org.springframework.web.filter.CorsFilter;

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
                .cors().and()
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ✅ Các endpoint cho phép public
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/register",
                                "/api/payment/return",     // Cho phép callback sau thanh toán
                                "/error"                   // Tránh lỗi Whitelabel /error
                        ).permitAll()

                        // ✅ Phân quyền các endpoint còn lại
                        .requestMatchers("/api/payment/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/employee/**").hasAnyRole("EMPLOYEE", "CUSTOMER")
                        .requestMatchers("/api/store/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers("/api/appointments/**").hasAnyRole("CUSTOMER", "EMPLOYEE")
                        .requestMatchers("/api/services/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .requestMatchers("/api/employees/store/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .requestMatchers("/api/working-time-slots/**").hasAnyRole("EMPLOYEE", "CUSTOMER")

                        // ✅ Cho phép preflight request từ trình duyệt
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Cho phép Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ✅ Mặc định cần xác thực
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

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

    @Bean
    public CorsFilter corsFilter() {
        System.out.println("Configuring CORS Filter...");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000"); // client
        config.addAllowedOrigin("http://localhost:3001"); // admin
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
