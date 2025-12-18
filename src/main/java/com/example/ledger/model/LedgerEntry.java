package com.example.ledger.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Data
@NoArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to Account
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    // Foreign key to Transaction
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EntryType type; // DEBIT or CREDIT

    // DECIMAL(19, 4) in MySQL for precision
    // Note: Amount is always positive here. The EntryType determines sign in calculation.
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Immutability enforced by making the setters non-existent (Lombok's @Data is sufficient)
    // and by setting 'updatable = false' on the 'createdAt' column.
    
    // Helper method for balance calculation logic:
    public BigDecimal getSignedAmount() {
        return type == EntryType.CREDIT ? amount : amount.negate();
    }

    public enum EntryType {
        DEBIT, CREDIT
    }
}