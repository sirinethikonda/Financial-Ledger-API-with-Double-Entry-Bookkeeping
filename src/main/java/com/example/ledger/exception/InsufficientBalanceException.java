package com.example.ledger.exception;

// Custom exception for business rule violation (overdraft prevention)
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}