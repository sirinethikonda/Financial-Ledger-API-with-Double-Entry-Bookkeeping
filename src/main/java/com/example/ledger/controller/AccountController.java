package com.example.ledger.controller;

import com.example.ledger.dto.CreateAccountRequest;
import com.example.ledger.model.Account;
import com.example.ledger.model.LedgerEntry;
import com.example.ledger.repository.LedgerEntryRepository;
import com.example.ledger.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final LedgerEntryRepository ledgerEntryRepository;

    public AccountController(AccountService accountService, LedgerEntryRepository ledgerEntryRepository) {
        this.accountService = accountService;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    // POST /accounts : Create a new user account.
    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account newAccount = accountService.createAccount(request);
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }

    // GET /accounts/{accountId} : Retrieve account details, including the calculated current balance.
    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable Long accountId) {
        Account account = accountService.getAccountWithBalance(accountId);
        return ResponseEntity.ok(account);
    }

    // GET /accounts/{accountId}/ledger-entries : Fetch a chronological list of all ledger entries for a specific account.
    @GetMapping("/{accountId}/ledger-entries")
    public ResponseEntity<List<LedgerEntry>> getLedgerEntries(@PathVariable Long accountId) {
        // Ensure account exists before fetching entries
        accountService.getAccountWithBalance(accountId); 
        List<LedgerEntry> entries = ledgerEntryRepository.findAllByAccountIdOrderByCreatedAtAsc(accountId);
        return ResponseEntity.ok(entries);
    }
}