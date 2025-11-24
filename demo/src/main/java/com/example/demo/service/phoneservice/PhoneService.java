package com.example.demo.service.phoneservice;

import com.example.demo.dto.UserAdditionalInfoDTO;
import com.example.demo.entity.Users.User;
import com.example.demo.entity.Users.UserLogin;
import com.example.demo.enums.AuthProvider;
import com.example.demo.repository.UserLoginRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.otp.OtpService;
import com.example.demo.service.rediservice.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PhoneService {

    private final RedisService redisService;
    private final OtpService otpService;
    private final UserRepository userRepository;
    private final UserLoginRepository userLoginRepository;
    private final JwtUtil jwtUtil;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OTP_PREFIX = "PHONEOTP_";

    // MSG91 Configuration
    private final String AUTH_KEY = "YOUR_KEY";
    private final String TEMPLATE_ID = "YOUR_TEMPLATE_ID";


    // 🔹 Step 1 ➝ Send OTP via MSG91
    public Map<String, Object> sendOtp(String phoneNumber) {
        Map<String, Object> response = new HashMap<>();

        String otp = otpService.generateOtp(OTP_PREFIX + phoneNumber, phoneNumber);

        String url = "https://api.msg91.com/api/v5/otp" +
                "?authkey=" + AUTH_KEY +
                "&mobile=" + phoneNumber +
                "&otp=" + otp +
                "&template_id=" + TEMPLATE_ID;

        try {
            restTemplate.getForEntity(url, String.class);
        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Failed to send OTP");
            return response;
        }

        response.put("status", true);
        response.put("message", "OTP sent");
        return response;
    }


    // 🔹 Step 2 ➝ OTP Verification / Login
    public Map<String, Object> verifyOtp(String phoneNumber, String otp) {
        Map<String, Object> response = new HashMap<>();

        if (!otpService.validateOtp(OTP_PREFIX + phoneNumber, phoneNumber, otp)) {
            response.put("status", false);
            response.put("message", "Invalid OTP");
            return response;
        }

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> createUser(phoneNumber));

        String token = jwtUtil.generateToken(
                user.getPhoneNumber(),
                user.getAuthProvider(),
                user.getFullName(),
                user.getCountry(),
                user.getCity(),
                user.getGender(),
                user.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getPhoneNumber());

        redisService.save(token, String.valueOf(user.getId()), jwtUtil.getTokenValidity());

        saveUserLogin(user, token, refreshToken);

        response.put("status", true);
        response.put("jwt", token);
        response.put("refreshToken", refreshToken);
        return response;
    }

    private User createUser(String phone) {
        User user = new User();
        user.setPhoneNumber(phone);
        user.setAuthProvider(AuthProvider.PHONE);
        return userRepository.save(user);
    }

    private void saveUserLogin(User user, String token, String refreshToken) {
        UserLogin login = userLoginRepository.findByUser(user)
                .orElse(new UserLogin());

        login.setUser(user);
        login.setJwtToken(token);
        login.setRefreshToken(refreshToken);
        login.setRefreshTokenExpiry(LocalDateTime.now().plusDays(30));
        userLoginRepository.save(login);
    }


    // 🔹 Step 3 ➝ Update Additional Info
    public Map<String, Object> updateUserInfo(String jwtToken, UserAdditionalInfoDTO dto) {
        Map<String, Object> response = new HashMap<>();

        Object userIdObj = redisService.get(jwtToken);
        if (userIdObj == null) {
            response.put("status", false);
            response.put("message", "Token expired / unauthorized");
            return response;
        }

        Long userId = Long.valueOf(userIdObj.toString());
        User user = userRepository.findById(userId).orElseThrow();

        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getCountry() != null) user.setCountry(dto.getCountry());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getDob() != null) user.setDob(dto.getDob());
        if (dto.getGender() != null) user.setGender(dto.getGender());

        userRepository.save(user);

        response.put("status", true);
        response.put("message", "User info updated");
        return response;
    }


    // 🔹 Step 4 ➝ Logout (Access Token)
    public Map<String, Object> logout(String jwtToken) {
        Map<String, Object> response = new HashMap<>();

        if (!redisService.exist(jwtToken)) {
            response.put("status", false);
            response.put("message", "Invalid token / Already logged out");
            return response;
        }

        redisService.delete(jwtToken);
        userLoginRepository.deleteByJwtToken(jwtToken);

        response.put("status", true);
        response.put("message", "Logged out successfully");
        return response;
    }
}
