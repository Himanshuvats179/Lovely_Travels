package com.example.demo.dto;

import com.example.demo.enums.Users.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAdditionalInfoDTO {
    private String fullName;
    private String country;
    private String city;
    private String phoneNumber;
    private Gender gender;
    private LocalDate dob;
}
