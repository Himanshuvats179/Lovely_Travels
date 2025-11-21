package com.example.demo.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String refreshHeader = request.getHeader("Refresh-Token");

        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        // --------------- Case 1: Access Token Valid ---------------
        if (accessToken != null && jwtUtil.isTokenValid(accessToken)) {

            Claims claims = jwtUtil.extractAllClaims(accessToken);
            String username = claims.get("sub", String.class);
            String role = claims.get("role", String.class);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username, null,
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
            return;
        }

        // --------------- Case 2: Access Token Invalid → Try Refresh Token ---------------
        if (accessToken != null && refreshHeader != null && jwtUtil.isRefreshTokenValid(refreshHeader)) {

            Claims refreshClaims = jwtUtil.extractAllClaims(refreshHeader);
            String email = refreshClaims.get("sub", String.class);
            String role = refreshClaims.get("role", String.class);
            // Generate new access token
            String newAccessToken = jwtUtil.createNewAccessToken(refreshHeader, role);

            // Send new token to client
            response.setHeader("New-Access-Token", newAccessToken);

            // Authenticate user
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email, null, List.of(new SimpleGrantedAuthority(role)));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
