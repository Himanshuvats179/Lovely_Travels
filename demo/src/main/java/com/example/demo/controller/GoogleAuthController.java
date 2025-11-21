package com.example.demo.controller;

import com.example.demo.service.GoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Google Authentication", description = "Endpoints for Google login/signup")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.google.scope}")
    private String scope;

    @GetMapping("/auth/google/login")
    public void loginWithGoogle(HttpServletResponse response) throws IOException {
        String googleScope = scope.replace(",", " ");
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + googleScope;

        // Automatically redirect the user's browser to Google login
        response.sendRedirect(googleAuthUrl);
    }


    // Step 2: Google callback
    @GetMapping(value = "/auth/google/callback")
    @Operation(summary = "Google OAuth2 callback", description = "Google redirects here with code, backend returns JWT")
    public Map<String, Object> googleCallback(@RequestParam("code") String code) {
        logger.info("Received Google OAuth2 callback with code: {}", code);

        // Authenticate and get JWT + user info
        Map<String, Object> jwtData = googleAuthService.authenticateWithGoogleCode(code);

        logger.info("Google authentication successful for email: {}", jwtData.get("email"));
        logger.debug("JWT Data: {}", jwtData);

        // Return the data as JSON directly
        return jwtData;
    }



}
