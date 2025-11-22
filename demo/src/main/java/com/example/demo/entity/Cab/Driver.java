package com.example.demo.entity.Cab;

import com.example.demo.enums.Cab.DriverStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

// Driver.java
@Entity
@Table(name = "drivers")
public class Driver {
    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "current_location")
    private String currentLocationWkt;

    @Enumerated(EnumType.STRING)
    private DriverStatus status;

    @Column(name = "last_ping")
    private Instant lastPing;
}

