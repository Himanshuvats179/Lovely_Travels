package com.example.demo.security;

import com.example.demo.enums.AuthProvider;
import com.example.demo.enums.Users.Gender;
import com.example.demo.repository.UserLoginRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {


    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long TOKEN_VALIDITY;

    @Value("${jwt.refreshExpiration}")
    private long REFRESH_TOKEN_VALIDITY;  // ADD THIS IN application.properties

    UserLoginRepository userLoginRepository;
    JwtUtil (UserLoginRepository userLoginRepository) {
        this.userLoginRepository = userLoginRepository;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ------------------- Access Token -------------------
    public String generateToken(
            String emailOrPhone,
            AuthProvider authProvider,
            String fullName,
            String country,
            String city,
            Gender gender,
            String role
    ) {

        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("authProvider", authProvider.name());

        header.put("alg", "HS256");



        Map<String, Object> claims = new HashMap<>();
        claims.put("name", fullName);
        claims.put("sub", emailOrPhone);
        claims.put("country", country);
        claims.put("city", city);
        claims.put("gender", gender);
        claims.put("iat", new Date().getTime());
        claims.put("exp", new Date().getTime() + TOKEN_VALIDITY);
        claims.put("role", role);

        return Jwts.builder()
                .setHeader(header)
                .setClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ------------------- Refresh Token -------------------
    public String generateRefreshToken(String emailOrPhone) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", emailOrPhone);
        claims.put("iat", new Date().getTime());
        claims.put("exp", new Date().getTime() + REFRESH_TOKEN_VALIDITY);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return new Date((Long) claims.get("exp")).after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String refreshToken) {
        return isTokenValid(refreshToken);
    }

    // Create new access token from refresh token
    public String createNewAccessToken(String refreshToken, String role) {
        Claims claims = extractAllClaims(refreshToken);

        String email = claims.get("sub", String.class);

        return Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setHeaderParam("authProvider", "REFRESH")
                .claim("sub", email)
                .claim("role", role)
                .claim("iat", new Date().getTime())
                .claim("exp", new Date().getTime() + TOKEN_VALIDITY)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public long getTokenValidity() {
        return  REFRESH_TOKEN_VALIDITY;
    }


    public boolean isAccessTokenValidFromDB(String token) {
        return isTokenValid(token) && userLoginRepository.findByJwtToken(token).isPresent();
    }

    public boolean isRefreshTokenValidFromDB(String token) {
        return isRefreshTokenValid(token) && userLoginRepository.findByRefreshToken(token).isPresent();
    }
}
