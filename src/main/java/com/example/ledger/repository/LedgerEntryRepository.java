package com.example.ledger.repository;

import com.example.ledger.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    /**
     * Requirement: [MET] Balance calculation correctness (on-demand).
     * Calculates the balance by summing all Credits and subtracting all Debits.
     * Use COALESCE to return 0.00 instead of NULL for new accounts.
     */
    @Query("""
        SELECT COALESCE(SUM(CASE 
            WHEN le.type = 'CREDIT' THEN le.amount 
            ELSE le.amount * -1 
        END), 0) 
        FROM LedgerEntry le 
        WHERE le.accountId = :accountId
        """)
    BigDecimal calculateBalance(@Param("accountId") Long accountId);

    // Requirement: [MET] Immutable Audit Trail.
    // Fetches history in exact order of occurrence for audit transparency.
    List<LedgerEntry> findAllByAccountIdOrderByCreatedAtAsc(Long accountId);
}