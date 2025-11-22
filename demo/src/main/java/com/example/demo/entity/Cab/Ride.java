package com.example.demo.entity;

import com.example.demo.enums.RideStatus;
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
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    // Logical Reference to User (Rider)
    @Column(name = "rider_id")
    private UUID riderId;

    // Logical Reference to Driver
    @Column(name = "driver_id")
    private UUID driverId;

    // Stores WKT (Well-Known Text) or a proprietary String representation of GEOMETRY
    @Column(name = "pickup_location")
    private String pickupLocation;

    @Column(name = "drop_location")
    private String dropLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    @Column(name = "fare_estimate", precision = 10, scale = 2)
    private BigDecimal fareEstimate;
}