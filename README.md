# 💳 IgirePay Payment Gateway

A comprehensive digital wallet and payment gateway system built with Java, JavaFX, and PostgreSQL. IgirePay provides a complete financial management platform with support for multiple account types, loans, disputes, and administrative controls.


## ✨ Features

### 🔐 Authentication & Authorization
- Secure user registration and login
- Role-based access control (Customer/Admin)
- PIN-based authentication
- Account lockout after failed login attempts
- First-time admin setup wizard

### 💰 Account Management
- **Wallet Accounts**: Instant transfers with no fees
- **Savings Accounts**: Interest-bearing with withdrawal limits
- Multiple accounts per customer
- Real-time balance updates
- Account creation and deletion with fund transfer

### 💸 Transactions
- Deposits and withdrawals
- Internal transfers between accounts
- External transfers to other customers
- Transaction history with filtering
- Idempotent transaction processing (duplicate prevention)
- Transaction status tracking

### 🏦 Loan Management
- Loan application and approval workflow
- Interest calculation
- Repayment tracking
- Overdue loan notifications
- Loan status management (Pending, Active, Overdue, Defaulted)

### 🔔 Notifications
- Real-time in-app notifications
- Transaction confirmations
- Loan reminders and overdue notices
- Dispute updates
- Unread notification badges

### 🛡️ Dispute Resolution
- Customer dispute filing
- Admin dispute management
- Status tracking (Pending, Investigating, Resolved, Rejected)
- Transaction freezing during investigation

### 📊 Reporting & Analytics
- Transaction reports with date filtering
- CSV export functionality
- Account balance summaries
- Loan portfolio overview
- Customer activity tracking

### 👨‍💼 Admin Features
- User management
- Account unlock capability
- Loan approval/rejection
- Dispute resolution
- System-wide reporting
- Customer promotion to admin

---

## 🏗️ Architecture

IgirePay follows a layered architecture pattern:

```
┌─────────────────────────────────────┐
│     Presentation Layer (UI)         │
│  ┌──────────────┬─────────────────┐ │
│  │   JavaFX     │  Console (CLI)  │ │
│  └──────────────┴─────────────────┘ │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│       Service Layer                  │
│  • AuthService                       │
│  • PaymentService                    │
│  • LoanService                       │
│  • AdminService                      │
│  • WithdrawalService                 │
│  • NotificationService               │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│       Data Access Layer (DAO)        │
│  • CustomerDAO                       │
│  • AccountDAO                        │
│  • TransactionDAO                    │
│  • LoanDAO                           │
│  • DisputeDAO                        │
│  • NotificationDAO                   │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│       Database (PostgreSQL)          │
└─────────────────────────────────────┘
```

### Design Patterns Used
- **DAO Pattern**: Separates data access logic from business logic
- **Singleton Pattern**: AppState for global state management
- **MVC Pattern**: Separation of concerns in UI layer
- **Factory Pattern**: Transaction and account creation
- **Strategy Pattern**: Different account types with polymorphic behavior

---

## 📦 Prerequisites

Before running IgirePay, ensure you have the following installed:

- **Java Development Kit (JDK) 17** or higher
  - [Download JDK](https://www.oracle.com/java/technologies/downloads/)
  
- **Apache Maven 3.8+**
  - [Download Maven](https://maven.apache.org/download.cgi)
  
- **PostgreSQL 15+**
  - [Download PostgreSQL](https://www.postgresql.org/download/)
  
- **Git** (optional, for cloning)
  - [Download Git](https://git-scm.com/downloads)

### Verify Installation

```bash
java -version    # Should show Java 17 or higher
mvn -version     # Should show Maven 3.8 or higher
psql --version   # Should show PostgreSQL 15 or higher
```

---

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/igirepay-payment-gateway.git
cd igirepay-payment-gateway
```

### 2. Set Up PostgreSQL Database

#### Create Database

```bash
psql -U postgres
```

```sql
CREATE DATABASE igirepay_db;
\q
```

#### Configure Database Connection

Edit `src/main/java/com/igirepay/igirepaypaymentgateway/LAB2/db/DatabaseConnection.java`:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/igirepay_db";
private static final String USER = "postgres";
private static final String PASSWORD = "your_password_here";
```

### 3. Build the Project

```bash
mvn clean install
```

---

## ▶️ Running the Application

### Option 1: JavaFX GUI Application (Recommended)

```bash
mvn javafx:run
```

**First Launch:**
- If no admin exists, you'll see the Admin Setup screen
- Create your first admin account
- Login with admin credentials

### Option 2: Console Application

Run the LAB3 console interface:

```bash
mvn exec:java -Dexec.mainClass="com.igirepay.igirepaypaymentgateway.LAB3.Main"
```

### Option 3: LAB Demos

**LAB 1 - OOP Concepts Demo:**
```bash
mvn exec:java -Dexec.mainClass="com.igirepay.igirepaypaymentgateway.LAB1.Main"
```

**LAB 2 - Database Integration Demo:**
```bash
mvn exec:java -Dexec.mainClass="com.igirepay.igirepaypaymentgateway.LAB2.Main"
```

---

## 📁 Project Structure

```
IgirePay Payment Gateway/
│
├── src/main/java/com/igirepay/igirepaypaymentgateway/
│   │
│   ├── LAB1/                          # OOP Fundamentals Demo
│   │   ├── model/                     # Domain models
│   │   │   ├── Account.java
│   │   │   ├── Customer.java
│   │   │   ├── SavingsAccount.java
│   │   │   ├── Transaction.java
│   │   │   └── WalletAccount.java
│   │   ├── service/
│   │   │   └── PaymentService.java
│   │   └── Main.java
│   │
│   ├── LAB2/                          # Database Integration
│   │   ├── dao/                       # Data Access Objects
│   │   │   ├── AccountDAO.java
│   │   │   ├── CustomerDAO.java
│   │   │   ├── DisputeDAO.java
│   │   │   ├── LoanDAO.java
│   │   │   ├── NotificationDAO.java
│   │   │   ├── ProcessedRequestDAO.java
│   │   │   ├── TransactionDAO.java
│   │   │   └── WithdrawalRequestDAO.java
│   │   ├── db/                        # Database utilities
│   │   │   ├── DatabaseConnection.java
│   │   │   └── SchemaSetup.java
│   │   ├── model/                     # Database models
│   │   ├── service/                   # Business logic
│   │   ├── util/
│   │   │   └── ValidationUtil.java
│   │   └── Main.java
│   │
│   ├── LAB3/                          # Advanced Features
│   │   ├── exception/
│   │   │   └── IgirePayException.java
│   │   ├── service/
│   │   │   ├── AdminService.java
│   │   │   ├── AuthService.java
│   │   │   ├── LoanService.java
│   │   │   ├── ReportService.java
│   │   │   └── WithdrawalService.java
│   │   ├── ui/                        # Console UI
│   │   ├── util/
│   │   │   └── AdminSetupUtil.java
│   │   └── Main.java
│   │
│   └── ui/                            # JavaFX GUI
│       ├── controller/                # FXML Controllers
│       │   ├── AccountsController.java
│       │   ├── AdminPanelController.java
│       │   ├── DashboardController.java
│       │   ├── DepositController.java
│       │   ├── LoginController.java
│       │   ├── RegisterController.java
│       │   ├── SendMoneyController.java
│       │   ├── TransactionsController.java
│       │   └── WithdrawController.java
│       ├── AppState.java              # Global state management
│       ├── IgirePayApp.java           # Main JavaFX application
│       └── SceneHelper.java           # Navigation helper
│
├── src/main/resources/
│   └── com/igirepay/igirepaypaymentgateway/ui/
│       ├── *.fxml                     # JavaFX layouts
│       └── *.css                      # Stylesheets
│
├── pom.xml                            # Maven configuration
├── README.md                          # This file
└── .gitignore
```

---

## 🗄️ Database Schema

### Core Tables

#### `customers`
```sql
- id (SERIAL PRIMARY KEY)
- full_name (VARCHAR)
- email (VARCHAR UNIQUE)
- phone_number (VARCHAR)
- pin_hash (VARCHAR)
- role (VARCHAR) -- 'CUSTOMER' or 'ADMIN'
- is_locked (BOOLEAN)
- failed_login_attempts (INT)
- created_at (TIMESTAMP)
```

#### `accounts`
```sql
- id (SERIAL PRIMARY KEY)
- customer_id (INT FOREIGN KEY)
- account_number (VARCHAR UNIQUE)
- account_type (VARCHAR) -- 'WALLET' or 'SAVINGS'
- balance (DECIMAL)
- currency (VARCHAR)
- created_at (TIMESTAMP)
```

#### `transactions`
```sql
- id (SERIAL PRIMARY KEY)
- account_id (INT FOREIGN KEY)
- transaction_type (VARCHAR)
- amount (DECIMAL)
- balance_before (DECIMAL)
- balance_after (DECIMAL)
- reference_id (VARCHAR UNIQUE)
- description (TEXT)
- status (VARCHAR)
- created_at (TIMESTAMP)
```

#### `loans`
```sql
- id (SERIAL PRIMARY KEY)
- customer_id (INT FOREIGN KEY)
- amount (DECIMAL)
- interest_rate (DECIMAL)
- duration_months (INT)
- amount_paid (DECIMAL)
- status (VARCHAR)
- approved_by (INT)
- disbursed_at (TIMESTAMP)
- due_date (DATE)
```

#### `disputes`
```sql
- id (SERIAL PRIMARY KEY)
- customer_id (INT FOREIGN KEY)
- transaction_id (INT FOREIGN KEY)
- dispute_type (VARCHAR)
- description (TEXT)
- status (VARCHAR)
- admin_notes (TEXT)
- resolved_by (INT)
- created_at (TIMESTAMP)
```

#### `notifications`
```sql
- id (SERIAL PRIMARY KEY)
- customer_id (INT FOREIGN KEY)
- title (VARCHAR)
- message (TEXT)
- type (VARCHAR)
- is_read (BOOLEAN)
- created_at (TIMESTAMP)
```

#### `withdrawal_requests`
```sql
- id (SERIAL PRIMARY KEY)
- customer_id (INT FOREIGN KEY)
- account_id (INT FOREIGN KEY)
- amount (DECIMAL)
- status (VARCHAR)
- created_at (TIMESTAMP)
```

#### `processed_requests`
```sql
- id (SERIAL PRIMARY KEY)
- reference_id (VARCHAR UNIQUE)
- processed_at (TIMESTAMP)
```

---

## 👥 User Roles

### Customer
- Register and login
- Create multiple accounts (Wallet/Savings)
- Deposit, withdraw, and transfer funds
- Apply for loans
- File disputes
- View transaction history
- Receive notifications

### Admin
- All customer capabilities
- Approve/reject loan applications
- Resolve disputes
- Unlock customer accounts
- View system-wide reports
- Promote users to admin
- Manage withdrawal requests

---

## 🔑 Key Concepts

### Idempotency
IgirePay implements idempotent transaction processing using the `processed_requests` table. Each transaction requires a unique `reference_id`. If the same `reference_id` is submitted again (e.g., due to network retry), the system rejects it as a duplicate.

```java
// Example: Duplicate detection
if (processedRequestDAO.existsByReferenceId(referenceId)) {
    System.out.println("DUPLICATE! Transaction already processed.");
    return false;
}
```

### Polymorphism
Different account types (Wallet, Savings) extend the base `Account` class and override methods like `deposit()` and `withdraw()` with type-specific behavior.

```java
// WalletAccount: No fees, no limits
// SavingsAccount: Withdrawal limits and fees apply
```

### Transaction Safety
All financial operations use database transactions with proper rollback on failure:

```java
try {
    conn.setAutoCommit(false);
    // Perform operations
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
}
```

### Security
- Passwords are hashed (not stored in plain text)
- SQL injection prevention using PreparedStatements
- Account lockout after 3 failed login attempts
- Role-based access control

---

## 📚 API Documentation

### PaymentService

#### `deposit(accountId, referenceId, amount, description)`
Deposits money into an account.

**Parameters:**
- `accountId` (int): Target account ID
- `referenceId` (String): Unique transaction reference
- `amount` (double): Amount to deposit
- `description` (String): Transaction description

**Returns:** `boolean` - Success status

---

#### `withdraw(accountId, referenceId, amount, description)`
Withdraws money from an account.

**Parameters:**
- `accountId` (int): Source account ID
- `referenceId` (String): Unique transaction reference
- `amount` (double): Amount to withdraw
- `description` (String): Transaction description

**Returns:** `boolean` - Success status

---

#### `transfer(fromAccountId, toAccountId, referenceId, amount, description)`
Transfers money between accounts.

**Parameters:**
- `fromAccountId` (int): Source account ID
- `toAccountId` (int): Destination account ID
- `referenceId` (String): Unique transaction reference
- `amount` (double): Amount to transfer
- `description` (String): Transaction description

**Returns:** `boolean` - Success status

---

### LoanService

#### `applyForLoan(customerId, accountId, amount, purpose)`
Submits a loan application.

**Parameters:**
- `customerId` (int): Applicant customer ID
- `accountId` (int): Disbursement account ID
- `amount` (double): Loan amount requested
- `purpose` (String): Loan purpose

**Returns:** `Loan` - Created loan object

**Throws:** `IgirePayException` - If validation fails

---

#### `approveLoan(loanId, adminId)`
Approves a pending loan application.

**Parameters:**
- `loanId` (int): Loan ID to approve
- `adminId` (int): Approving admin's ID

**Returns:** `boolean` - Success status

**Throws:** `IgirePayException` - If not authorized or loan not found

---

### AuthService

#### `login(email, pin)`
Authenticates a user.

**Parameters:**
- `email` (String): User email
- `pin` (String): User PIN

**Returns:** `Customer` - Authenticated customer object

**Throws:** `IgirePayException` - If credentials invalid or account locked

---

#### `register(fullName, email, phoneNumber, pin)`
Registers a new customer.

**Parameters:**
- `fullName` (String): Customer's full name
- `email` (String): Unique email address
- `phoneNumber` (String): Phone number (078/079 format)
- `pin` (String): 4-digit PIN

**Returns:** `Customer` - Created customer object

**Throws:** `IgirePayException` - If validation fails

---



## 🙏 Acknowledgments

- JavaFX community for excellent UI framework
- PostgreSQL team for robust database system
- Maven for dependency management
- All contributors and testers

---

## 📞 Support

For support, email support@igirepay.com or open an issue in the GitHub repository.

---

## 🗺️ Roadmap

- [ ] Mobile app (Android/iOS)
- [ ] REST API for third-party integrations
- [ ] Multi-currency support
- [ ] Biometric authentication
- [ ] Scheduled payments
- [ ] Bill payment integration
- [ ] QR code payments
- [ ] Transaction analytics dashboard
- [ ] Email notifications
- [ ] Two-factor authentication (2FA)

---

**Made with ❤️ in Rwanda**
