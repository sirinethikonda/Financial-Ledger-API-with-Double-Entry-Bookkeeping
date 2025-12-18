package com.example.ledger.repository;

import com.example.ledger.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // We add a locking mechanism for concurrent updates in the service layer
    // Not needed on the Repository itself, as Spring Data JPA handles the find/save.
}