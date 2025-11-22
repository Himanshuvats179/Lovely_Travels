package com.example.demo.entity.Hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

// InventoryId.java (Composite Key for Inventory)
@Data
@Embeddable
public class InventoryId implements Serializable {
    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "date")
    private LocalDate date;
    // ... equals() and hashCode()
}
