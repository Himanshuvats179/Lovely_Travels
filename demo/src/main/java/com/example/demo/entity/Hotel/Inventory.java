package com.example.demo.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.Type; // For JSONB and GEOGRAPHY types

// Inventory.java (Critical for high concurrency)
@Entity
@Table(name = "inventory")
public class Inventory {
    // Composite Primary Key recommended: room_id + date
    @EmbeddedId
    private InventoryId id; // Class for composite key

    @Column(name = "total_count")
    private int totalCount;

    @Column(name = "reserved_count")
    private int reservedCount;

    @Version // JPA Optimistic Locking mechanism
    private int version;

    // ... Getters, Setters
}

