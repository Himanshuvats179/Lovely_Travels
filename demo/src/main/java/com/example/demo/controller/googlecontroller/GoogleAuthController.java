package com.example.demo.controller.googlecontroller;

import com.example.demo.dto.UserAdditionalInfoDTO;
import com.example.demo.service.googleservice.GoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.HashMap;
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


        response.sendRedirect(googleAuthUrl);
    }


    @GetMapping(value = "/auth/google/callback")
    @Operation(summary = "Google OAuth2 callback", description = "Google redirects here with code, backend returns JWT")
    public Map<String, Object> googleCallback(@RequestParam("code") String code) {
        logger.info("Received Google OAuth2 callback with code: {}", code);


        Map<String, Object> jwtData = googleAuthService.authenticateWithGoogleCode(code);

        logger.info("Google authentication successful for email: {}", jwtData.get("email"));
        logger.debug("JWT Data: {}", jwtData);


        return jwtData;
    }
    @PostMapping("/user/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();
        logger.info("Logout API called");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Authorization header missing or invalid: {}", authHeader);
            response.put("message", "Authorization header missing or invalid");
            response.put("success", false);
            response.put("status", 401);
            return ResponseEntity.status(401).body(response);
        }

        String token = authHeader.substring(7);
        logger.info("Extracted Bearer token: {}", token);

        boolean result = googleAuthService.logoutUser(token);
        logger.info("Logout service result: {}", result);

        if (result) {
            logger.info("User logged out successfully for token: {}", token);
            response.put("message", "Logged out successfully");
            response.put("success", true);
            response.put("status", 200);
            return ResponseEntity.ok(response);
        }

        logger.warn("Invalid token or already logged out: {}", token);
        response.put("message", "Invalid token or already logged out");
        response.put("success", false);
        response.put("status", 401);
        return ResponseEntity.status(401).body(response);
    }



    @PostMapping("/user/extra-info")
    public ResponseEntity<Map<String, Object>> extraInfo(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UserAdditionalInfoDTO requestDTO) {

        Map<String, Object> response = new HashMap<>();

        // Validate JWT and get user email
        String email = googleAuthService.validateTokenAndGetEmail(authHeader);
        if (email == null) {
            response.put("success", false);
            response.put("message", "Invalid or missing token");
            return ResponseEntity.status(401).body(response);
        }

        // Call service method to save/update extra info
        boolean updated = googleAuthService.updateExtraInfo(email, requestDTO);

        if (updated) {
            response.put("success", true);
            response.put("message", "Extra info saved successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Failed to save extra info");
            return ResponseEntity.status(500).body(response);
        }
    }



}
