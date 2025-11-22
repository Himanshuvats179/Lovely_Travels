package com.example.demo.entity.Hotel;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId; // FK to Hotel table

    @Column(nullable = false)
    private String type; // e.g., 'Deluxe', 'Suite'

    @Column(name = "base_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal basePrice;
}