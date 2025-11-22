package com.example.demo.entity;

import lombok.*;

// This is NOT a JPA entity; it's a POJO to represent the JSON structure.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}