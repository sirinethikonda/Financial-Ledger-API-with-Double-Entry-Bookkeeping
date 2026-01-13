# Ledger API(a Financial Ledger API with Double-Entry Bookkeeping):
a Financial Ledger API with Double-Entry Bookkeeping is a robust, ledger-based banking and payment processing API built with Spring Boot 3 and Java 17.

This project was engineered to manage a complex, real-worldfinancial domain model, using a double-entry bookkeeping system for transaction integrity and audibility. It accurately handles core banking features, ensuring every movement of money is accounted for through a permanent audit trail.

including:

* Cash deposits and transfers
* Real-time transaction processing
* Calculated Balance Integrity
* Immutable Audit Trail & Ledger Audibility

 **Index**

  * The main purpose of the project
  * Features
  * Project structure
  * Technical highlights
  * Design Decisions (ACID & Integrity)
  * Setup & Execution
 
**The main purpose of the project**

My goal is to highlight my ability to create complex systems with strict business logic. That's what inspired the decision for a ledger-based system. While having a simple balance column in the database would have been much easier to implement, a ledger-based approach is far more robust as it provides an immutable history of every transaction, making it audit-ready and resilient to data corruption.

**Features**

* Double-Entry Bookkeeping: Every transaction (Deposit or Transfer)         generates balanced debit and credit entries in the ledger for full auditability.
* Real-time Balance Calculation: Balances are calculated on-demand by summing ledger entries (SUM(amount)), ensuring they are always a direct reflection of the transaction history.
* Insufficient Funds Prevention: Atomic checks at the service level prevent accounts from reaching negative balances.
* Fully Dockerized: Ready-to-use docker-compose.yml for the application and MySQL 8.0 database.
* Global Exception Handling: Custom business exceptions (like InsufficientBalanceException) return clean, informative API responses with appropriate HTTP status codes.


**Project structure**

```
main 
ledger-api/
├── src/main/java/com/example/ledger/
│   ├── LedgerApiApplication.java
│   │
│   ├── model/
│   │   ├── Account.java
│   │   ├── Transaction.java
│   │   └── LedgerEntry.java            # Immutable record of credit/debit.
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

```

---

## 📸 API Demonstration

#### 1. Create Ledger Account
Initializes a new account with a specific currency and status.  
<img src="./screenshots/Screenshot%202026-01-13%20002550.png" width="400" alt="Create Account">  
** Endpoint: `POST /api/accounts`**

---

#### 2. Cash Deposit
Adds funds to an account by creating a Credit entry in the ledger.  
<img src="./screenshots/Screenshot%202026-01-13%20002607.png" width="400" alt="Deposit">  
** Endpoint: `POST /api/transactions/deposit`**

---

#### 3. Internal Transfer
Moves money between accounts using atomic transactions (Debit Source + Credit Destination).  
<img src="./screenshots/Screenshot%202026-01-13%20002707.png" width="400" alt="Transfer">  
** Endpoint: `POST /api/transactions/transfer`**

---

#### 4. Withdrawal
Debits an account after performing a thread-safe balance validation.  
<img src="./screenshots/Screenshot%202026-01-13%20002736.png" width="400" alt="Withdrawal">  
**Endpoint: `POST /api/transactions/withdrawal`**

---

#### 5. Account Balance & Audit Trail
On-demand balance calculation and chronological list of all ledger entries for transparency.  
<img src="./screenshots/Screenshot%202026-01-13%20002813.png" width="400" alt="Balance">  
<img src="./screenshots/Screenshot%202026-01-13%20002759.png" width="400" alt="Audit Trail">  
**Endpoints: `GET /api/accounts/{id}` and `GET /api/accounts/{id}/ledger-entries`**

---

**Technical highlights**

* Stack: Java 17, Spring Boot 3, MySQL 8, Docker.
* Precision: Uses BigDecimal for all monetary calculations to avoid  floating-point errors common with Double.
* Containerization: Fully containerized with a Dockerfile and docker-compose.yml for a "one-command" setup.
* ACID Integrity: Uses Spring’s @Transactional to ensure that if one "leg" of a transfer fails, the entire transaction is rolled back, maintaining atomicity.


**Design Decisions**

Double-Entry Implementation
For every Transfer request:

* A record is created in the transactions table to document the event.
* A Debit entry (negative amount) is added to ledger_entries for the source account.
* A Credit entry (positive amount) is added to ledger_entries for the destination account.
* The sum of these entries for the specific transaction ID must equal zero.
                        
**ACID Properties**

* Atomicity: Guaranteed by @Transactional. Both ledger entries must succeed, or none will be persisted.
* Consistency: The system ensures the ledger is always balanced and follows the rules defined in AccountType and Status enums.
* Isolation: Uses the default RDBMS isolation level to handle concurrent transfer requests safely, preventing race conditions.
* Durability: All records are persisted in a Docker-managed MySQL volume.

**Balance Calculation & Prevention**

Balance is marked as @Transient in the Account model. It is calculated via a query: SELECT SUM(amount) FROM ledger_entry WHERE account_id = :id. Negative Balance Prevention: Before any debit, the service calculates the current balance. If CurrentBalance < RequestedAmount, an InsufficientBalanceException is thrown, triggering a rollback.

**Setup & Execution**

*Prerequisites:
* Docker Desktop

* Maven 3.8+

* Java 17 (JRE)

###1. Build the Project
* Open your terminal in the project root and run:


    mvn clean package -DskipTests
    
###2. Run with Docker
 
    docker-compose up --build -d  
    
**The API will be available at http://localhost:8080/api and the database at localhost:3307**

**API Testing (Postman) :**
   A Postman collection is recommended for testing. Key endpoints include:
     
     POST /api/accounts: Create a new ledger account.
     POST /api/transactions/deposit: Add funds to an account (Credit).
     POST /api/transactions/transfer: Move funds between accounts (Debit + Credit).
