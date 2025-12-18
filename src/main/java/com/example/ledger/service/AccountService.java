package com.example.ledger.service;

import com.example.ledger.dto.CreateAccountRequest;
import com.example.ledger.model.Account;
import com.example.ledger.repository.AccountRepository;
import com.example.ledger.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public AccountService(AccountRepository accountRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setUserId(request.getUserId());
        account.setType(request.getType());
        account.setCurrency(request.getCurrency());
        // Status defaults to ACTIVE
        return accountRepository.save(account);
    }

    public Account getAccountWithBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found with ID: " + accountId));
        
        // Balance calculation logic
        BigDecimal balance = ledgerEntryRepository.calculateBalance(accountId);
        account.setBalance(balance != null ? balance : BigDecimal.ZERO);
        return account;
    }
}