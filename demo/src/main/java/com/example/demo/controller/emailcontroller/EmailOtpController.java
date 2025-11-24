//package com.example.demo.controller.emailcontroller;
//
//import com.example.demo.service.rediservice.RedisService;
//import com.example.demo.service.emailservice.EmailService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@RestController
//@RequestMapping("/auth/otp")
//@Tag(name = "Email OTP Auth", description = "Handles OTP based email authentication")
//public class EmailOtpController {
//
//    private final EmailService emailService;
//    private final RedisService redisService;
//
//    private static final String TEMP_EMAIL_KEY = "PENDING_EMAIL_";
//
//    public EmailOtpController(EmailService emailService, RedisService redisService) {
//        this.emailService = emailService;
//        this.redisService = redisService;
//    }
//
//
//    @Operation(summary = "Send OTP", description = "Send OTP to email for verification")
//    @PostMapping("/send")
//    public ResponseEntity<Map<String, Object>> sendOtp(@RequestParam String email) {
//        Map<String, Object> response = new HashMap<>();
//        log.info("OTP send request received for email: {}", email);
//
//        // Validate email format
//        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
//            response.put("success", false);
//            response.put("message", "Invalid email format!");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        // Call service to send OTP
//        Map<String, Object> otpResponse = emailService.sendOtpResponse(email);
//
//        // Store email status as PENDING (Expire after 10 minutes)
//        redisService.save(TEMP_EMAIL_KEY + email, "PENDING", 10);
//
//        return ResponseEntity.ok(otpResponse);
//    }
//
//    // Step-3 : Verify OTP
//    @Operation(summary = "Verify OTP", description = "Verify OTP sent to email")
//    @PostMapping("/verify")
//    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestParam String email,
//                                                         @RequestParam String otp) {
//        Map<String, Object> response = new HashMap<>();
//        boolean valid = emailService.validateOtp(email, otp);
//
//        if (!valid) {
//            response.put("success", false);
//            response.put("message", "Invalid or Expired OTP");
//            return ResponseEntity.status(401).body(response);
//        }
//
//        log.info("OTP verified for {}", email);
//        //redisService.delete(TEMP_EMAIL_KEY + email); // cleanup
//
//        response.put("success", true);
//        response.put("message", "OTP Verified! Email Authentication Successful.");
//        return ResponseEntity.ok(response);
//    }
//}


package com.example.demo.controller.emailcontroller;


import com.example.demo.controller.googlecontroller.GoogleAuthController;
import com.example.demo.dto.UserAdditionalInfoDTO;
import com.example.demo.service.rediservice.RedisService;
import com.example.demo.service.emailservice.EmailService;

import com.example.demo.service.otp.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth/otp")
@Tag(name = "Email OTP Auth", description = "Handles OTP based email authentication and registration")
public class EmailOtpController {

    private final EmailService emailService;
    private final RedisService redisService;
    private final OtpService otpService;
    // private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthController.class);
    private static final String OTP_SESSION_KEY = "OTP_SESSION_";
    private static final String TEMP_REG_TOKEN_KEY = "TEMP_REGISTRATION_TOKEN_";

    public EmailOtpController(EmailService emailService,
                              RedisService redisService,
                              OtpService otpService) {
        this.emailService = emailService;
        this.redisService = redisService;
        this.otpService = otpService;

    }

    // ---------------- Step 1: Send OTP ----------------
    @Operation(summary = "Send OTP", description = "Send OTP to email for verification")
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        log.info("OTP send request received for email: {}", email);

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            response.put("success", false);
            response.put("message", "Invalid email format!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Generate OTP and send
            String otp = otpService.generateOtp("EMAIL", email);
            emailService.sendEmail(email, otp);

            // Generate OTP session token
            String otpToken = UUID.randomUUID().toString();
            redisService.save(OTP_SESSION_KEY + otpToken, email, 5); // valid 5 min

            response.put("success", true);
            response.put("message", "OTP sent to email");
            response.put("otpToken", otpToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ---------------- Step 2: Verify OTP ----------------
    @Operation(summary = "Verify OTP", description = "Verify OTP using otpToken")
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> req) {
        Map<String, Object> response = new HashMap<>();

        String otpToken = req.get("otpToken");
        String otp = req.get("otp");

        String tokenKey = OTP_SESSION_KEY + otpToken;
        if (!redisService.exist(tokenKey)) {
            response.put("success", false);
            response.put("message", "Invalid or expired OTP session token");
            return ResponseEntity.status(401).body(response);
        }

        String email = redisService.get(tokenKey).toString();
        boolean valid = otpService.validateOtp("EMAIL", email, otp);

        if (!valid) {
            response.put("success", false);
            response.put("message", "Invalid or expired OTP");
            return ResponseEntity.status(401).body(response);
        }



        // OTP verified, delete OTP session
        redisService.delete(tokenKey);

        return  ResponseEntity.ok( emailService.registerUser(email));
    }

    // ---------------- Step 3: Register User ----------------

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

        boolean result = emailService.logoutUser(token);
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
        String email = emailService.validateTokenAndGetEmail(authHeader);
        if (email == null) {
            response.put("success", false);
            response.put("message", "Invalid or missing token");
            return ResponseEntity.status(401).body(response);
        }

        // Call service method to save/update extra info
        boolean updated = emailService.updateExtraInfo(email, requestDTO);

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