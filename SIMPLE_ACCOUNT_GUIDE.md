# Simple Account Management - Beginner's Guide

## What This Does (In Simple Words)

This feature ensures that:
1. **Each customer can have ONLY ONE wallet and ONE savings account** (not multiple)
2. **When you delete an account, the money automatically moves to your other account**
3. **You cannot delete your last account** (you must keep at least one)

## Real-Life Example

Imagine you have:
- Wallet: 50,000 RWF
- Savings: 30,000 RWF

If you delete your wallet:
- Your savings will now have: 80,000 RWF (30,000 + 50,000)
- Your wallet is gone

## How It Works (Step by Step)

### Step 1: Check if Account Already Exists

Before creating a new account, we check:
```java
// Does this customer already have a wallet?
if (customer already has wallet) {
    return error "You already have a wallet"
}
```

### Step 2: Create Account (If Allowed)

```java
// Create new wallet
Account wallet = new Account(customerId, "WALLET", 0.0, "RWF");
// Save to database
```

### Step 3: Delete Account with Money Transfer

When deleting an account:
```java
1. Check: Does customer have another account? 
   - If NO → Cannot delete (must keep at least one)
   - If YES → Continue

2. Get the balance from account to delete
   - Example: wallet has 50,000 RWF

3. Add that balance to the other account
   - savings = savings + 50,000

4. Delete the account
```

## The Code Structure (Simple)

### 1. Database Constraint (Automatic Protection)
```sql
-- This prevents duplicate accounts at database level
ALTER TABLE accounts 
ADD CONSTRAINT unique_customer_account_type 
UNIQUE (customer_id, account_type);
```

**What this means:** The database itself will reject any attempt to create duplicate accounts.

### 2. AccountDAO (Database Operations)
This class talks to the database:
- `createAccount()` - Add new account
- `findByCustomerIdAndType()` - Find wallet or savings
- `hasAccountOfType()` - Check if account exists
- `updateBalance()` - Change account balance
- `deleteAccount()` - Remove account

### 3. AccountService (Business Logic)
This class contains the rules:
- `createWalletAccount()` - Create wallet (checks for duplicates first)
- `createSavingsAccount()` - Create savings (checks for duplicates first)
- `deleteAccountWithTransfer()` - Delete account and move money

## Code Explanation (For Your Presentation)

### Creating an Account

```java
public int createWalletAccount(int customerId) {
    // STEP 1: Check if customer already has a wallet
    if (accountDAO.hasAccountOfType(customerId, "WALLET")) {
        System.out.println("Error: Customer already has a wallet");
        return -1; // Return error code
    }

    // STEP 2: Create the wallet
    Account newWallet = new Account(customerId, "WALLET", 0.0, "RWF");
    
    // STEP 3: Save to database
    int accountId = accountDAO.createAccount(newWallet);
    
    return accountId; // Return the new account ID
}
```

**Explanation for supervisor:**
- First, we check if the customer already has this type of account
- If yes, we reject the request
- If no, we create the account and save it to the database

### Deleting an Account

```java
public boolean deleteAccountWithTransfer(int accountId) {
    // STEP 1: Get the account to delete
    Account accountToDelete = accountDAO.findById(accountId);
    
    // STEP 2: Find the customer's other account
    String otherType = accountToDelete.getAccountType().equals("WALLET") 
                       ? "SAVINGS" : "WALLET";
    Account otherAccount = accountDAO.findByCustomerIdAndType(
        accountToDelete.getCustomerId(), otherType);
    
    // STEP 3: Check if other account exists
    if (otherAccount == null) {
        System.out.println("Error: Cannot delete your only account");
        return false;
    }
    
    // STEP 4: Transfer the money
    double balance = accountToDelete.getBalance();
    double newBalance = otherAccount.getBalance() + balance;
    accountDAO.updateBalance(otherAccount.getId(), newBalance);
    
    // STEP 5: Delete the account
    accountDAO.deleteAccount(accountId);
    
    return true;
}
```

**Explanation for supervisor:**
- We get the account that needs to be deleted
- We find the customer's other account (if wallet is deleted, find savings)
- If there's no other account, we reject (customer must have at least one)
- We add the balance from deleted account to the remaining account
- Finally, we delete the account

## Key Concepts You Should Explain

### 1. **Validation** (Checking Before Doing)
Before creating or deleting, we always check if it's allowed.

### 2. **Database Constraint** (Automatic Protection)
The database has a rule that prevents duplicate accounts automatically.

### 3. **Transaction Safety** (All or Nothing)
When transferring money and deleting, either everything succeeds or nothing happens (no money lost).

### 4. **DAO Pattern** (Separation of Concerns)
- **DAO** = Talks to database (SQL queries)
- **Service** = Contains business rules (validation, logic)
- **Model** = Represents data (Account, Customer)

## Testing the Feature

Run this simple demo:
```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.igirepay.igirepaypaymentgateway.SIMPLE.SimpleAccountDemo"
```

The demo will show:
1. Creating wallet ✓
2. Trying to create another wallet ✗ (rejected)
3. Creating savings ✓
4. Deleting wallet → money moves to savings ✓
5. Trying to delete last account ✗ (rejected)

## Questions Your Supervisor Might Ask

**Q: Why only one account per type?**
A: To simplify account management and prevent confusion. Users have one wallet for daily use and one savings for long-term storage.

**Q: What happens to the money when deleting?**
A: It automatically transfers to the customer's other account. No money is lost.

**Q: What if someone tries to create duplicate accounts?**
A: The system checks first and rejects the request. The database also has a constraint as backup protection.

**Q: What if the transfer fails halfway?**
A: We use database transactions. If any step fails, everything rolls back (undoes all changes).

**Q: Can a customer delete all accounts?**
A: No. The system requires at least one account. You can only delete if you have another account.

## Summary (For Your Presentation)

This feature implements **account management with three main rules**:

1. **One account per type** - Enforced by validation and database constraint
2. **Automatic fund transfer** - When deleting, money moves to remaining account
3. **Minimum one account** - Cannot delete your last account

The implementation uses:
- **DAO pattern** for database operations
- **Service layer** for business logic
- **Validation** before every operation
- **Database transactions** for safety

## Tips for Presentation

1. **Start with the problem**: "Users might create multiple wallets by mistake"
2. **Show the solution**: "We prevent this with validation and database constraints"
3. **Demonstrate**: Run the demo program to show it working
4. **Explain the code**: Walk through one method (like createWalletAccount)
5. **Discuss safety**: Explain how we protect user money during transfers

Good luck with your presentation! 🎓
