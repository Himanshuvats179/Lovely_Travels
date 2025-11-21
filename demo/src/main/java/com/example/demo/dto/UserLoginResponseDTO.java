package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginResponseDTO {

    private String name  ;
    private String jwtToken;
    private String tokenExpiry;
    private Long userId;
    private String email ;
    private String phoneNumber;
}
