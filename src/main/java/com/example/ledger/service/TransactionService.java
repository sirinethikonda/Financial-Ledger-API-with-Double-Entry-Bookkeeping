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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static com.example.ledger.model.LedgerEntry.EntryType.CREDIT;
import static com.example.ledger.model.LedgerEntry.EntryType.DEBIT;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, 
                              LedgerEntryRepository ledgerEntryRepository, 
                              AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Transaction executeTransfer(TransferRequest request) {
        // [FIXED] Pessimistic locking to prevent race conditions during balance check
        Account source = accountRepository.findByIdWithLock(request.getSourceAccountId())
                .orElseThrow(() -> new NoSuchElementException("Source Account not found."));
        Account dest = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new NoSuchElementException("Destination Account not found."));

        if (!source.getCurrency().equals(dest.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch.");
        }

        Transaction transaction = createPendingTransaction(TransactionType.TRANSFER, request.getAmount(), 
                source.getCurrency(), request.getDescription(), source.getId(), dest.getId());

        try {
            logger.info("Processing transfer: {} from {} to {}", request.getAmount(), source.getId(), dest.getId());
            
            BigDecimal currentBalance = ledgerEntryRepository.calculateBalance(source.getId());
            BigDecimal newBalance = currentBalance.subtract(request.getAmount());

            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                throw new InsufficientBalanceException("Insufficient funds for Account " + source.getId());
            }

            createLedgerEntry(source.getId(), transaction.getId(), DEBIT, request.getAmount());
            createLedgerEntry(dest.getId(), transaction.getId(), CREDIT, request.getAmount());

            transaction.setStatus(TransactionStatus.COMPLETED);
            return transactionRepository.save(transaction);
        } catch (Exception e) {
            logger.error("Transfer failed: {}", e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw e; 
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Transaction executeWithdrawal(AmountRequest request) {
        // [FIXED] Lock account row before calculating balance
        Account source = accountRepository.findByIdWithLock(request.getAccountId())
                .orElseThrow(() -> new NoSuchElementException("Account not found."));

        Transaction transaction = createPendingTransaction(TransactionType.WITHDRAWAL, request.getAmount(), 
                source.getCurrency(), request.getDescription(), source.getId(), null);
        
        BigDecimal currentBalance = ledgerEntryRepository.calculateBalance(source.getId());
        BigDecimal newBalance = currentBalance.subtract(request.getAmount());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new InsufficientBalanceException("Insufficient funds.");
        }

        createLedgerEntry(source.getId(), transaction.getId(), DEBIT, request.getAmount());
        transaction.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction executeDeposit(AmountRequest request) {
        Account dest = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new NoSuchElementException("Account not found."));
        
        Transaction transaction = createPendingTransaction(TransactionType.DEPOSIT, request.getAmount(), 
                dest.getCurrency(), request.getDescription(), null, dest.getId());

        createLedgerEntry(dest.getId(), transaction.getId(), CREDIT, request.getAmount());
        transaction.setStatus(TransactionStatus.COMPLETED);
        return transactionRepository.save(transaction);
    }

    private Transaction createPendingTransaction(TransactionType type, BigDecimal amount, String currency, String description, Long sourceId, Long destId) {
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

    private void createLedgerEntry(Long accountId, Long transactionId, LedgerEntry.EntryType type, BigDecimal amount) {
        LedgerEntry entry = new LedgerEntry();
        entry.setAccountId(accountId);
        entry.setTransactionId(transactionId);
        entry.setType(type);
        entry.setAmount(amount);
        ledgerEntryRepository.save(entry);
    }
}