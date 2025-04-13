package com.otp.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.otp.model.User;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // Генерация ключа
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(
            "NevermoreNevermoreNevermoreNevermoreNeve".getBytes() // минимум 32 байта
    );

    // Время жизни токена — 1 час
    private static final long EXPIRATION_TIME_MS = 3600000;

    public static boolean isAdminToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class)
                .equals("admin");
    }

    public static String generateToken(User user) {
        System.out.println("Генерация токена началась");

        String jwt = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("Генерация токена завершена: " + jwt);
        return jwt;
    }

    public static int getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("user_id", Integer.class);
    }

    public static String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // subject — это username
    }

}

