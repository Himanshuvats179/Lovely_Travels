package com.example.demo.service.emailservice;

import com.example.demo.service.otp.OtpService;
import com.example.demo.service.rediservice.RedisService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final OtpService otpService;
    private final RedisService redisService;

    private static final long OTP_COOLDOWN_MINUTES = 1; // 1 minute cooldown
    private static final String OTP_COOLDOWN_PREFIX = "EMAIL_COOLDOWN";

    public EmailService(JavaMailSender mailSender, OtpService otpService, RedisService redisService) {
        this.mailSender = mailSender;
        this.otpService = otpService;
        this.redisService = redisService;
    }


    public String sendOtp(String email) {
        String cooldownKey = OTP_COOLDOWN_PREFIX + "_" + email;

        // Check if cooldown exists
        if (redisService.exist(cooldownKey)) {
            throw new RuntimeException("OTP already sent. Please wait a minute before requesting again.");
        }

        // Generate OTP
        String otp = otpService.generateOtp("EMAIL", email);

        // Send OTP via email
        sendEmail(email, otp);

        // Set cooldown in Redis
        redisService.save(cooldownKey, "WAIT", OTP_COOLDOWN_MINUTES);

        return otp;
    }


    private void sendEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + "\nIt will expire in 5 minutes.");
        mailSender.send(message);
    }


    public boolean validateOtp(String email, String otp) {
        return otpService.validateOtp("EMAIL", email, otp);
    }

    public Map<String, Object> sendOtpResponse(String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            sendOtp(email);
            response.put("success", true);
            response.put("message", "OTP sent successfully");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

}
