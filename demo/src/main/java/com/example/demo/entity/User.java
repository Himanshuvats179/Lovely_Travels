package com.example.demo.entity;

import com.example.demo.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String email; // optional for OTP login

    private String password; // optional for Google / OTP login

    @Column(unique = true)
    private String phoneNumber; // optional for Email / Google login

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private boolean isActive = true;

    private String country;

    private String city;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate dob;


    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "BYTEA")
    private byte[] profileImage=new byte[0];

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider = AuthProvider.EMAIL;

    public void setIsActive(boolean b) {
        this.isActive = b;
    }
}
