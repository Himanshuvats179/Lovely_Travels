package com.example.demo.entity.Hotel;

import jakarta.persistence.*;

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

