package com.example.demo.controller.emailcontroller;

import com.example.demo.service.rediservice.RedisService;
import com.example.demo.service.emailservice.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth/otp")
@Tag(name = "Email OTP Auth", description = "Handles OTP based email authentication")
public class EmailOtpController {

    private final EmailService emailService;
    private final RedisService redisService;

    private static final String TEMP_EMAIL_KEY = "PENDING_EMAIL_";

    public EmailOtpController(EmailService emailService, RedisService redisService) {
        this.emailService = emailService;
        this.redisService = redisService;
    }


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

        // Call service to send OTP
        Map<String, Object> otpResponse = emailService.sendOtpResponse(email);

        // Store email status as PENDING (Expire after 10 minutes)
        redisService.save(TEMP_EMAIL_KEY + email, "PENDING", 10);

        return ResponseEntity.ok(otpResponse);
    }

    // Step-3 : Verify OTP
    @Operation(summary = "Verify OTP", description = "Verify OTP sent to email")
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestParam String email,
                                                         @RequestParam String otp) {
        Map<String, Object> response = new HashMap<>();
        boolean valid = emailService.validateOtp(email, otp);

        if (!valid) {
            response.put("success", false);
            response.put("message", "Invalid or Expired OTP");
            return ResponseEntity.status(401).body(response);
        }

        log.info("OTP verified for {}", email);
        redisService.delete(TEMP_EMAIL_KEY + email); // cleanup

        response.put("success", true);
        response.put("message", "OTP Verified! Email Authentication Successful.");
        return ResponseEntity.ok(response);
    }
}
