package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    // Logical Reference to User
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String provider; // 'Stripe', 'PayPal'

    // The tokenized card reference
    @Column(nullable = false)
    private String token;
}