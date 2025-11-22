package com.example.demo.entity.Payment;

import com.example.demo.enums.Hotel.ReferenceType;
import jakarta.persistence.*;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

// Transaction.java (Often made Immutable after creation)
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    // Use scale and precision annotations for DECIMAL mapping
    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    private String currency;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private ReferenceType referenceType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
}

