package com.example.ledger.controller;

import com.example.ledger.dto.AmountRequest;
import com.example.ledger.dto.TransferRequest;
import com.example.ledger.model.Transaction;
import com.example.ledger.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // POST /transfers : Execute a financial transfer between two internal accounts.
    @PostMapping("/transfers")
    public ResponseEntity<Transaction> executeTransfer(@Valid @RequestBody TransferRequest request) {
        Transaction transaction = transactionService.executeTransfer(request);
        return new ResponseEntity<>(transaction, HttpStatus.ACCEPTED);
    }

    // POST /deposits : Simulate a deposit into an account.
    @PostMapping("/deposits")
    public ResponseEntity<Transaction> executeDeposit(@Valid @RequestBody AmountRequest request) {
        Transaction transaction = transactionService.executeDeposit(request);
        return new ResponseEntity<>(transaction, HttpStatus.ACCEPTED);
    }

    // POST /withdrawals : Simulate a withdrawal from an account.
    @PostMapping("/withdrawals")
    public ResponseEntity<Transaction> executeWithdrawal(@Valid @RequestBody AmountRequest request) {
        Transaction transaction = transactionService.executeWithdrawal(request);
        return new ResponseEntity<>(transaction, HttpStatus.ACCEPTED);
    }
}