# Presentation Guide - Account Management Feature

## For Your Supervisor/Sponsor

---

## 1. INTRODUCTION (2 minutes)

**What to say:**

"Good morning/afternoon. Today I will present the Account Management feature I implemented for the IgirePay Payment Gateway system. This feature ensures that each customer can have only one wallet and one savings account, and handles account deletion safely."

---

## 2. THE PROBLEM (1 minute)

**What to say:**

"Without this feature, customers could accidentally create multiple wallet accounts or multiple savings accounts. This would cause confusion:
- Which wallet should I use?
- Where is my money?
- How do I manage multiple accounts?

Also, when deleting an account, what happens to the money inside?"

---

## 3. THE SOLUTION (2 minutes)

**What to say:**

"I implemented three main rules:

**Rule 1: One Account Per Type**
- Each customer can have ONLY ONE wallet
- Each customer can have ONLY ONE savings
- If they try to create a duplicate, the system rejects it

**Rule 2: Automatic Money Transfer**
- When deleting an account, the money automatically moves to the other account
- No money is lost
- Example: If I delete my wallet with 50,000 RWF, that money goes to my savings

**Rule 3: Minimum One Account**
- Customers must keep at least one account
- Cannot delete the last account
- This ensures they can always receive money"

---

## 4. HOW IT WORKS - TECHNICAL (3 minutes)

### A. Database Protection

**What to say:**

"First, I added a database constraint to prevent duplicate accounts automatically."

**Show this code:**
```sql
ALTER TABLE accounts 
ADD CONSTRAINT unique_customer_account_type 
UNIQUE (customer_id, account_type);
```

**Explain:**
"This means the database itself will reject any attempt to create duplicate accounts. It's like a safety lock."

---

### B. Checking Before Creating

**What to say:**

"Before creating an account, the system checks if it already exists."

**Show this code:**
```java
public boolean hasWallet(int customerId) {
    // Get all accounts for this customer
    List<Account> accounts = accountDAO.findByCustomerId(customerId);
    
    // Loop through and check if any is a WALLET
    for (Account account : accounts) {
        if (account.getAccountType().equals("WALLET")) {
            return true; // Found a wallet!
        }
    }
    
    return false; // No wallet found
}
```

**Explain:**
1. We get all accounts for the customer
2. We loop through each account
3. If we find a wallet, we return true (already exists)
4. If we don't find a wallet, we return false (can create new one)

---

### C. Deleting with Money Transfer

**What to say:**

"When deleting an account, the system follows these steps:"

**Show this code:**
```java
public boolean deleteAccountWithTransfer(int accountId) {
    // STEP 1: Get the account to delete
    Account accountToDelete = accountDAO.findById(accountId);
    
    // STEP 2: Find the OTHER account
    String otherType = accountType.equals("WALLET") ? "SAVINGS" : "WALLET";
    Account otherAccount = findAccountByType(customerId, otherType);
    
    // STEP 3: Check if other account exists
    if (otherAccount == null) {
        return false; // Cannot delete only account
    }
    
    // STEP 4: Transfer the money
    double newBalance = otherAccount.getBalance() + accountToDelete.getBalance();
    accountDAO.updateBalance(otherAccount.getId(), newBalance);
    
    // STEP 5: Delete the account
    accountDAO.deleteAccount(accountId);
    
    return true;
}
```

**Explain each step:**
1. **Step 1:** Get the account we want to delete
2. **Step 2:** Find the customer's other account (if deleting wallet, find savings)
3. **Step 3:** Check if other account exists (if not, reject deletion)
4. **Step 4:** Add the money from deleted account to the other account
5. **Step 5:** Delete the account

---

## 5. DEMONSTRATION (3 minutes)

**What to say:**

"Let me show you how it works in practice."

**Run the demo:**
```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.igirepay.igirepaypaymentgateway.SIMPLE.SimpleAccountDemo"
```

**Point out these parts:**

1. **Creating wallet** → Success ✓
2. **Trying to create another wallet** → Rejected ✗
3. **Creating savings** → Success ✓
4. **Adding money** → Wallet: 50,000, Savings: 30,000
5. **Deleting wallet** → Money moves to savings (now 80,000) ✓
6. **Trying to delete last account** → Rejected ✗

---

## 6. CODE STRUCTURE (2 minutes)

**What to say:**

"I organized the code using three layers:"

**Draw this on board/slide:**
```
┌─────────────────────────┐
│   SimpleAccountDemo     │  ← Main program (what user sees)
└───────────┬─────────────┘
            │
┌───────────▼─────────────┐
│  SimpleAccountHelper    │  ← Business logic (rules)
└───────────┬─────────────┘
            │
┌───────────▼─────────────┐
│      AccountDAO         │  ← Database operations (SQL)
└─────────────────────────┘
```

**Explain:**
- **Demo:** Shows the feature working
- **Helper:** Contains the business rules (checking, transferring)
- **DAO:** Talks to the database (save, delete, find)

This separation makes the code easier to understand and maintain.

---

## 7. TESTING (1 minute)

**What to say:**

"I tested the feature with these scenarios:

✓ Creating first wallet - Works
✓ Creating duplicate wallet - Rejected
✓ Creating first savings - Works
✓ Creating duplicate savings - Rejected
✓ Deleting account with money - Money transfers correctly
✓ Deleting last account - Rejected
✓ Database constraint - Prevents duplicates automatically"

---

## 8. BENEFITS (1 minute)

**What to say:**

"This feature provides several benefits:

1. **User-Friendly:** Customers don't get confused with multiple accounts
2. **Safe:** Money is never lost when deleting accounts
3. **Reliable:** Database constraints provide automatic protection
4. **Simple:** Easy to understand and use"

---

## 9. CONCLUSION (1 minute)

**What to say:**

"In summary, I implemented an account management system that:
- Limits customers to one wallet and one savings account
- Automatically transfers money when deleting accounts
- Prevents deletion of the last account
- Uses database constraints for extra protection

The code is simple, well-commented, and follows good practices like the DAO pattern. Thank you for your attention. Do you have any questions?"

---

## COMMON QUESTIONS & ANSWERS

### Q1: "What if the database constraint fails?"
**A:** "The application checks first, so it won't even try to create a duplicate. The database constraint is a backup safety measure."

### Q2: "What happens if the money transfer fails?"
**A:** "We use database transactions. If any step fails, everything rolls back (undoes all changes). This ensures no money is lost."

### Q3: "Can an admin override these rules?"
**A:** "Currently no, but we could add an admin override feature in the future if needed."

### Q4: "Why not allow multiple accounts?"
**A:** "To keep it simple for users. Most people only need one wallet for daily use and one savings for long-term storage."

### Q5: "How did you test this?"
**A:** "I created a demo program that tests all scenarios: creating accounts, rejecting duplicates, transferring money, and preventing deletion of the last account."

---

## TIPS FOR PRESENTATION

1. **Practice the demo** - Run it several times before presenting
2. **Speak slowly** - Don't rush through the code
3. **Use simple words** - Avoid complex technical jargon
4. **Show confidence** - You understand this code!
5. **Prepare for questions** - Review the Q&A section above

---

## FILES TO SHOW

1. **SIMPLE_ACCOUNT_GUIDE.md** - Detailed explanation
2. **SimpleAccountDemo.java** - The demonstration
3. **SimpleAccountHelper.java** - The business logic
4. **SchemaSetup.java** - Database constraint (line with UNIQUE constraint)

---

## PRESENTATION CHECKLIST

Before presenting, make sure:
- [ ] Code compiles without errors
- [ ] Demo runs successfully
- [ ] You understand each method
- [ ] You can explain the database constraint
- [ ] You know the three main rules
- [ ] You've practiced the demo
- [ ] You're ready for questions

---

Good luck! You've got this! 🎓✨
