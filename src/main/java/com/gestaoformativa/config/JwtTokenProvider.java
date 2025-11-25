package com.gestaoformativa.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key jwtSecret;
    private final int jwtExpirationInMs;

    public JwtTokenProvider(
            @Value("${app.jwt-secret}") String jwtSecret,
            @Value("${app.jwt-expiration-ms}") int jwtExpirationInMs) {

        // Converter a string para uma chave segura
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(jwtSecret, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            // Log assinatura JWT inválida
        } catch (MalformedJwtException ex) {
            // Token JWT malformado
        } catch (ExpiredJwtException ex) {
            // Token JWT expirado
        } catch (UnsupportedJwtException ex) {
            // Token JWT não suportado
        } catch (IllegalArgumentException ex) {
            // Claims JWT vazias
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}