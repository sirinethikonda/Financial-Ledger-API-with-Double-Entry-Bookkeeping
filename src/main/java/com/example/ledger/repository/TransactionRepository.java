package com.example.ledger.repository;

import com.example.ledger.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Professional addition: allows auditors to see all transactions for one account
    List<Transaction> findBySourceAccountIdOrDestinationAccountId(Long sourceId, Long destId);
}