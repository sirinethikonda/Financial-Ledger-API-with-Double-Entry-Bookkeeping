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

    // Immutability: JPA's save() is used only for INSERT. No update/delete methods exposed.

    // Core Business Logic: Calculate the account balance on demand.
    @Query("""
        SELECT SUM(CASE 
            WHEN le.type = 'CREDIT' THEN le.amount 
            ELSE le.amount * -1 
        END) 
        FROM LedgerEntry le 
        WHERE le.accountId = :accountId
        """)
    BigDecimal calculateBalance(@Param("accountId") Long accountId);

    // Endpoint: Fetch a chronological list of all ledger entries for a specific account.
    List<LedgerEntry> findAllByAccountIdOrderByCreatedAtAsc(Long accountId);
}