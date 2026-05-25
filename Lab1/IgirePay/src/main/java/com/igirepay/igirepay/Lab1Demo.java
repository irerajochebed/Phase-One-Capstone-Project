package com.igirepay.igirepay;

import com.igirepay.igirepay.model.*;
import com.igirepay.igirepay.model.PaymentManager;

import java.time.LocalDate;

/**
 * ═════════════════════════════════════════════════════════════════════════════
 *                    LAB 1 COMPLETE DEMONSTRATION
 * Building Secure Data-Driven JavaFX Applications with JDBC & OOP
 * 
 * Exercise 1.1: Design classes with Inheritance & Encapsulation ✓
 * Exercise 1.2: Implement Polymorphism in transaction processing ✓
 * Exercise 1.3: Use Java Collections for data management ✓
 * 
 * ═════════════════════════════════════════════════════════════════════════════
 */
public class Lab1Demo {

    public static void main(String[] args) {
        System.out.println("\n" +
                "╔═════════════════════════════════════════════════════════════════╗\n" +
                "║              IgirePay Payment Management System                 ║\n" +
                "║                    LAB 1 - COMPLETE DEMO                        ║\n" +
                "║                 OOP, Inheritance & Collections                  ║\n" +
                "╚═════════════════════════════════════════════════════════════════╝\n");

        // ═════════════════════════════════════════════════════════════════════════════
        // EXERCISE 1.1: CREATE OBJECTS DEMONSTRATING INHERITANCE & ENCAPSULATION
        // ═════════════════════════════════════════════════════════════════════════════
        System.out.println("\n" +
                "┌─────────────────────────────────────────────────────────────────┐\n" +
                "│ EXERCISE 1.1: INHERITANCE & ENCAPSULATION                       │\n" +
                "│ Creating Account objects from different subclasses              │\n" +
                "└─────────────────────────────────────────────────────────────────┘\n");

        // Create Customer 1 with Wallet Account
        Customer customer1 = new Customer("CUST-001", "Alice Johnson", 
                "alice@igirepay.com", "+250788123456");
        System.out.println("👤 Customer Created: " + customer1.getFullName());
        System.out.println(customer1.toString());

        WalletAccount aliceWallet = new WalletAccount("WAL-001", 50000.0, 
                "+250788123456", "1234");
        System.out.println("\n💳 Wallet Account Created for Alice:");
        System.out.println(aliceWallet.toString());

        // Create Customer 2 with Savings Account
        Customer customer2 = new Customer("CUST-002", "Bob Smith", 
                "bob@igirepay.com", "+250789654321");
        System.out.println("\n👤 Customer Created: " + customer2.getFullName());
        System.out.println(customer2.toString());

        SavingsAccount bobSavings = new SavingsAccount("SAV-001", 200000.0, 
                0.05, 500000.0, LocalDate.now().plusYears(2));
        System.out.println("\n💰 Savings Account Created for Bob:");
        System.out.println(bobSavings.toString());

        // Create Customer 3 with multiple accounts
        Customer customer3 = new Customer("CUST-003", "Carol White", 
                "carol@igirepay.com", "+250787654321");
        System.out.println("\n👤 Customer Created: " + customer3.getFullName());

        WalletAccount carolWallet = new WalletAccount("WAL-002", 75000.0, 
                "+250787654321", "5678");
        SavingsAccount carolSavings = new SavingsAccount("SAV-002", 300000.0, 
                0.06, 1000000.0, LocalDate.now().plusYears(3));

        System.out.println("\n💳 Wallet Account Created for Carol:");
        System.out.println(carolWallet.toString());
        System.out.println("\n💰 Savings Account Created for Carol:");
        System.out.println(carolSavings.toString());

        // ═════════════════════════════════════════════════════════════════════════════
        // EXERCISE 1.2: DEMONSTRATE POLYMORPHISM
        // ═════════════════════════════════════════════════════════════════════════════
        System.out.println("\n" +
                "┌─────────────────────────────────────────────────────────────────┐\n" +
                "│ EXERCISE 1.2: POLYMORPHISM                                      │\n" +
                "│ Different account types handle operations differently           │\n" +
                "└─────────────────────────────────────────────────────────────────┘\n");

        System.out.println("--- WALLET ACCOUNT OPERATIONS (Instant, No Fees) ---\n");
        aliceWallet.deposit(10000.0);
        aliceWallet.withdraw(5000.0);

        System.out.println("\n--- SAVINGS ACCOUNT OPERATIONS (Withdrawal Limits & Fees) ---\n");
        bobSavings.deposit(50000.0);
        bobSavings.withdraw(10000.0);  // Fee: 2%
        bobSavings.withdraw(8000.0);   // Fee: 2%
        bobSavings.withdraw(6000.0);   // Fee: 2%
        bobSavings.withdraw(4000.0);   // This should FAIL (limit reached)

        // ═════════════════════════════════════════════════════════════════════════════
        // EXERCISE 1.3: JAVA COLLECTIONS DEMONSTRATION
        // ═════════════════════════════════════════════════════════════════════════════
        System.out.println("\n" +
                "┌─────────────────────────────────────────────────────────────────┐\n" +
                "│ EXERCISE 1.3: JAVA COLLECTIONS                                  │\n" +
                "│ - List: Customer & Transaction management                       │\n" +
                "│ - Set: Duplicate detection using Reference IDs                  │\n" +
                "│ - Map: Failed transaction logging                               │\n" +
                "└─────────────────────────────────────────────────────────────────┘\n");

        PaymentManager paymentManager = new PaymentManager();

        // Add accounts to payment manager
        paymentManager.addAccount("CUST-001", aliceWallet);
        paymentManager.addAccount("CUST-002", bobSavings);
        paymentManager.addAccount("CUST-003", carolWallet);

        System.out.println("\n✅ All accounts registered with PaymentManager\n");

        // ═════════════════════════════════════════════════════════════════════════════
        // SCENARIO 1: Normal Transaction Processing
        // ═════════════════════════════════════════════════════════════════════════════
        System.out.println("\n" +
                "═══════════════════════════════════════════════════════════════════\n" +
                "SCENARIO 1: Normal Transaction Processing\n" +
                "═══════════════════════════════════════════════════════════════════\n");

        Transaction txn1 = new Transaction("TXN-001", "REF-20240525-001", 15000.0,
                Transaction.TransactionType.SEND);
        System.out.println("Processing: " + txn1.toString() + "\n");
        String result1 = paymentManager.processTransaction("CUST-001", txn1);
        System.out.println("Result: " + result1);

        // ═════════════════════════════════════════════════════════════════════════════
        // SCENARIO 2: Duplicate Transaction (KEY CONCEPT: Idempotency)
        // ═════════════════════════════════════════════════════════════════════════════
        System.out.println("\n" +
                "═══════════════════════════════════════════════════════════════════\n" +
                "SCENARIO 2: DUPLICATE TRANSACTION - Network Retry\n" +
                "This is the CORE PROBLEM the system solves!\n" +
                "═══════════════════════════════════════════════════════════════════\n");

        Transaction txn2 = new Transaction("TXN-002", "REF-20240525-001", 15000.0,
                Transaction.TransactionType.SEND);
        System.out.println("Retrying same transaction (same Reference ID):");
        System.out.println("Processing: " + txn2.toString() + "\n");
        String result2 = paymentManager.processTransaction("CUST-001", txn2);
        System.out.println("Result: " + result2);
        System.out.println("\n💡 KEY TAKEAWAY: The Set<String> processedReferenceIds " +
                "prevents duplicate charges!");

        // ═════════════════════════════════════════════════════════════════════════════
        // SCENARIO 3: Multiple Valid Transactions
        // ═════════════════════════════════════════════════════════════════════════════
        System.out.println("\n" +
                "═══════════════════════════════════════════════════════════════════\n" +
                "SCENARIO 3: Multiple Unique Transactions\n" +
                "═══════════════════════════════════════════════════════════════════\n");

        Transaction txn3 = new Transaction("TXN-003", "REF-20240525-002", 30000.0,
                Transaction.TransactionType.DEPOSIT);
        String result3 = paymentManager.processTransaction("CUST-002", txn3);
        System.out.println("Result: " + result3);

        Transaction txn4 = new Transaction("TXN-004", "REF-20240525-003", 20000.0,
                Transaction.TransactionType.SEND);
        String result4 = paymentManager.processTransaction("CUST-003", txn4);
        System.out.println("Result: " + result4);

        // ═════════════════════════════════════════════════════════════════════════════
        // SCENARIO 4: Failed Transaction (Insufficient Balance)
        // ═════════════════════════════════════════════════════════════════════════════
        System.out.println("\n" +
                "═══════════════════════════════════════════════════════════════════\n" +
                "SCENARIO 4: Failed Transaction (Insufficient Balance)\n" +
                "═══════════════════════════════════════════════════════════════════\n");

        Transaction txn5 = new Transaction("TXN-005", "REF-20240525-004", 999999.0,
                Transaction.TransactionType.SEND);
        String result5 = paymentManager.processTransaction("CUST-001", txn5);
        System.out.println("Result: " + result5);

        // ═════════════════════════════════════════════════════════════════════════════
        // DISPLAY FINAL REPORTS
        // ═════════════════════════════════════════════════════════════════════════════
        System.out.println("\n" +
                "═══════════════════════════════════════════════════════════════════\n" +
                "FINAL REPORTS\n" +
                "═══════════════════════════════════════════════════════════════════\n");

        paymentManager.displayAllAccounts();
        paymentManager.displayTransactionHistory();
        paymentManager.displayProcessedReferenceIds();
        paymentManager.displayFailedTransactions();

        // ═════════════════════════════════════════════════════════════════════════════
        // SUMMARY AND KEY TAKEAWAYS
        // ═════════════════════════════════════════════════════════════════════════════
        System.out.println("\n" +
                "╔═════════════════════════════════════════════════════════════════╗\n" +
                "║                    KEY TAKEAWAYS - LAB 1                        ║\n" +
                "╚═════════════════════════════════════════════════════════════════╝\n");

        System.out.println("✅ EXERCISE 1.1: INHERITANCE & ENCAPSULATION");
        System.out.println("   • Account is an abstract base class");
        System.out.println("   • WalletAccount & SavingsAccount inherit from Account");
        System.out.println("   • Each class has fields, constructors, getters/setters, toString()");

        System.out.println("\n✅ EXERCISE 1.2: POLYMORPHISM");
        System.out.println("   • withdraw() and deposit() behave differently per account type");
        System.out.println("   • WalletAccount: Instant transfers, no fees");
        System.out.println("   • SavingsAccount: Withdrawal limits (3/month), 2% fees");
        System.out.println("   • processTransaction() demonstrates runtime polymorphism");

        System.out.println("\n✅ EXERCISE 1.3: JAVA COLLECTIONS");
        System.out.println("   • Map<String, Account>: Fast account lookup by customer ID");
        System.out.println("   • List<Transaction>: Track transaction history in order");
        System.out.println("   • Set<String>: Detect duplicate transactions by reference ID");
        System.out.println("   • List<Transaction>: Log failed transactions");

        System.out.println("\n💡 IDEMPOTENCY CONCEPT:");
        System.out.println("   Network timeouts cause client retries → duplicate requests");
        System.out.println("   Reference IDs in a Set prevent processing the same transaction twice");
        System.out.println("   This ensures financial data integrity and prevents duplicate charges!");

        System.out.println("\n" +
                "╔═════════════════════════════════════════════════════════════════╗\n" +
                "║                    LAB 1 COMPLETE! ✓                           ║\n" +
                "╚═════════════════════════════════════════════════════════════════╝\n");
    }
}
