# Ledger API(a Financial Ledger API with Double-Entry Bookkeeping):
a Financial Ledger API with Double-Entry Bookkeeping is a robust, ledger-based banking and payment processing API built with Spring Boot 3 and Java 17.

This project was engineered to manage a complex, real-worldfinancial domain model, using a double-entry bookkeeping system for transaction integrity and audibility. It accurately handles core banking features, ensuring every movement of money is accounted for through a permanent audit trail.

including:

* Cash deposits and transfers
* Real-time transaction processing
 
 **Index**

 The main purpose of the project
 Features
 Project structure
 Technical highlights
 Design Decisions (ACID & Integrity)
 Setup & Execution
 
**The main purpose of the project**

My goal is to highlight my ability to create complex systems with strict business logic. That's what inspired the decision for a ledger-based system. While having a simple balance column in the database would have been much easier to implement, a ledger-based approach is far more robust as it provides an immutable history of every transaction, making it audit-ready and resilient to data corruption.

**Features**

* Double-Entry Bookkeeping: Every transaction (Deposit or Transfer)         generates balanced debit and credit entries in the ledger for full auditability.
* Real-time Balance Calculation: Balances are calculated on-demand by summing ledger entries (SUM(amount)), ensuring they are always a direct reflection of the transaction history.
* Insufficient Funds Prevention: Atomic checks at the service level prevent accounts from reaching negative balances.
* Fully Dockerized: Ready-to-use docker-compose.yml for the application and MySQL 8.0 database.
* Global Exception Handling: Custom business exceptions (like InsufficientBalanceException) return clean, informative API responses with appropriate HTTP status codes.


**Project structure**

ledger-api/
├── src/main/java/com/example/ledger/
│   ├── LedgerApiApplication.java
│   │
│   ├── model/
│   │   ├── Account.java
│   │   ├── Transaction.java
│   │   └── LedgerEntry.java            # IMPORTANT: Immutable record of credit/debit.
│   │
│   ├── repository/
│   │   ├── AccountRepository.java
│   │   ├── TransactionRepository.java
│   │   └── LedgerEntryRepository.java         # Calculates balance via SUM query.
│   │
│   ├── service/
│   │   ├── AccountService.java
│   │   └── TransactionService.java            #  Handles @Transactional & ACID logic.
│   │
│   ├── controller/
│   │   ├── AccountController.java
│   │   └── TransactionController.java
│   │
│   ├── dto/
│   │   ├── CreateAccountRequest.java
│   │   ├── TransferRequest.java
│   │   └── AmountRequest.java
│   │
│   └── exception/
│       ├── InsufficientBalanceException.java   #  Prevents negative balances.
│       └── GlobalExceptionHandler.java
│
├── src/main/resources/
│   └── application.yml
│
├── Dockerfile
├── docker-compose.yml
└── pom.xml                          
