package com.example.serversideclinet.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

//@Component
//public class JwtUtil {
//    private String secret = "your-256-bit-secret";
//    private long expirationTime = 86400000; // 24h
//
//
//
//    public String generateToken(UserDetails userDetails) {
//        System.out.println("Generating token for: " + userDetails.getUsername());
//        Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();
//        System.out.println("Roles in JwtUtil: " + roles);
//
//        String token = JWT.create()
//                .withSubject(userDetails.getUsername())
//                .withClaim("roles", roles.stream()
//                        .map(GrantedAuthority::getAuthority)
//                        .collect(Collectors.toList()))
//                .withIssuedAt(new Date(System.currentTimeMillis()))
//                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
//                .sign(Algorithm.HMAC512(secret));
//        System.out.println("Token generated: " + token);
//        return token;
//    }
//
//    public String getUsernameFromToken(String token) {
//        return JWT.decode(token).getSubject();
//    }
//
//
//
//    public boolean validateToken(String token, UserDetails userDetails) {
//        try {
//            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secret)).build();
//            DecodedJWT decodedJWT = verifier.verify(token);
//            final String username = decodedJWT.getSubject();
//            return (username.equals(userDetails.getUsername()) && !isTokenExpired(decodedJWT));
//        } catch (JWTVerificationException e) {
//            return false;
//        }
//    }
//
//    private boolean isTokenExpired(DecodedJWT decodedJWT) {
//        return decodedJWT.getExpiresAt().before(new Date());
//    }
//}

@Component
public class JwtUtil {
    private String secret = "your-256-bit-secret";
    private long expirationTime = 86400000; // 24h

    public String generateToken(UserDetails userDetails, Integer userId) {
        System.out.println("Generating token for: " + userDetails.getUsername());
        Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();
        System.out.println("Roles in JwtUtil: " + roles);

        String token = JWT.create()
                .withSubject(userDetails.getUsername())
                .withClaim("roles", roles.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .withClaim("userId", userId)
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(Algorithm.HMAC512(secret));
        System.out.println("Token generated: " + token);
        return token;
    }

    public String getUsernameFromToken(String token) {
        return JWT.decode(token).getSubject();
    }

    public Integer getUserIdFromToken(String token) {
        return JWT.decode(token).getClaim("userId").asInt();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(secret)).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            final String username = decodedJWT.getSubject();
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(decodedJWT));
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private boolean isTokenExpired(DecodedJWT decodedJWT) {
        return decodedJWT.getExpiresAt().before(new Date());
    }
}
