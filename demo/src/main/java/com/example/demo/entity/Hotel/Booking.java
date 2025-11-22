package com.example.demo.entity.Hotel;

import com.example.demo.enums.Payment.BookingStatus;
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
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    // Logical Reference (No DB constraint)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId; // FK to Hotel table

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;
}