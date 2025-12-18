package com.example.ledger.service;

import com.example.ledger.dto.AmountRequest;
import com.example.ledger.dto.TransferRequest;
import com.example.ledger.exception.InsufficientBalanceException;
import com.example.ledger.model.Account;
import com.example.ledger.model.LedgerEntry;
import com.example.ledger.model.Transaction;
import com.example.ledger.model.Transaction.TransactionStatus;
import com.example.ledger.model.Transaction.TransactionType;
import com.example.ledger.repository.AccountRepository;
import com.example.ledger.repository.LedgerEntryRepository;
import com.example.ledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static com.example.ledger.model.LedgerEntry.EntryType.CREDIT;
import static com.example.ledger.model.LedgerEntry.EntryType.DEBIT;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository, LedgerEntryRepository ledgerEntryRepository, 
                              AccountRepository accountRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    /**
     * Executes a transfer (debit/credit) between two internal accounts.
     * Uses a single database transaction to ensure Atomicity.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED) // Ensures data consistency
    public Transaction executeTransfer(TransferRequest request) {
        Account source = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new NoSuchElementException("Source Account not found."));
        Account dest = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new NoSuchElementException("Destination Account not found."));

        if (!source.getCurrency().equals(dest.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch between accounts.");
        }

        // 1. Create PENDING Transaction Record
        Transaction transaction = createPendingTransaction(
                TransactionType.TRANSFER, 
                request.getAmount(), 
                source.getCurrency(), 
                request.getDescription(), 
                source.getId(), 
                dest.getId()
        );

        try {
            // *** CRITICAL STEP: Balance Integrity Check ***
            // Must check balance BEFORE creating the debit entry.
            BigDecimal currentBalance = ledgerEntryRepository.calculateBalance(source.getId());
            BigDecimal newBalance = (currentBalance != null ? currentBalance : BigDecimal.ZERO).subtract(request.getAmount());

            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                throw new InsufficientBalanceException("Account ID " + source.getId() + " has insufficient funds for transfer.");
            }

            // 2. Double-Entry Bookkeeping: Create two immutable Ledger Entries
            createLedgerEntry(source.getId(), transaction.getId(), DEBIT, request.getAmount());
            createLedgerEntry(dest.getId(), transaction.getId(), CREDIT, request.getAmount());

            // 3. Mark Transaction as COMPLETED
            transaction.setStatus(TransactionStatus.COMPLETED);
            return transactionRepository.save(transaction);
            
            // The method finishes, and the Spring transaction manager COMMITS all 3 database operations (1x Transaction update + 2x LedgerEntry inserts) atomically.
        } catch (InsufficientBalanceException | IllegalArgumentException | NoSuchElementException e) {
            // Set status to FAILED and re-throw, allowing Spring to rollback if it hasn't already.
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction); // This update will be rolled back if re-thrown, but for clarity/logging we set it.
            throw e; 
        } catch (Exception e) {
            // Catch other DB/runtime errors
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new RuntimeException("Transaction failed due to system error: " + e.getMessage(), e);
        }
    }

    // Simplified DEPOSIT logic (requires no balance check)
    @Transactional
    public Transaction executeDeposit(AmountRequest request) {
        Account dest = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new NoSuchElementException("Destination Account not found."));
        
        Transaction transaction = createPendingTransaction(
                TransactionType.DEPOSIT, 
                request.getAmount(), 
                dest.getCurrency(), 
                request.getDescription(), 
                null, 
                dest.getId()
        );

        // Deposit is a simple credit to the account
        createLedgerEntry(dest.getId(), transaction.getId(), CREDIT, request.getAmount());
        
        transaction.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(transaction);
    }
    
    // Simplified WITHDRAWAL logic (requires balance check)
    @Transactional
    public Transaction executeWithdrawal(AmountRequest request) {
        Account source = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new NoSuchElementException("Source Account not found."));

        Transaction transaction = createPendingTransaction(
                TransactionType.WITHDRAWAL, 
                request.getAmount(), 
                source.getCurrency(), 
                request.getDescription(), 
                source.getId(), 
                null
        );
        
        // *** CRITICAL STEP: Balance Integrity Check ***
        BigDecimal currentBalance = ledgerEntryRepository.calculateBalance(source.getId());
        BigDecimal newBalance = (currentBalance != null ? currentBalance : BigDecimal.ZERO).subtract(request.getAmount());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new InsufficientBalanceException("Account ID " + source.getId() + " has insufficient funds for withdrawal.");
        }

        // Withdrawal is a simple debit from the account
        createLedgerEntry(source.getId(), transaction.getId(), DEBIT, request.getAmount());

        transaction.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(transaction);
    }

    private Transaction createPendingTransaction(
            TransactionType type, BigDecimal amount, String currency, String description, Long sourceId, Long destId) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setDescription(description);
        transaction.setSourceAccountId(sourceId);
        transaction.setDestinationAccountId(destId);
        transaction.setStatus(TransactionStatus.PENDING);
        return transactionRepository.save(transaction);
    }

    private LedgerEntry createLedgerEntry(
            Long accountId, Long transactionId, LedgerEntry.EntryType type, BigDecimal amount) {
        LedgerEntry entry = new LedgerEntry();
        entry.setAccountId(accountId);
        entry.setTransactionId(transactionId);
        entry.setType(type);
        entry.setAmount(amount);
        return ledgerEntryRepository.save(entry);
    }
}