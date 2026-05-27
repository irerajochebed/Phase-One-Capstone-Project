package com.igirepay.igirepaypaymentgateway.LAB1.service;

import com.igirepay.igirepaypaymentgateway.LAB1.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB1.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB1.model.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PaymentService is the BRAIN of the system.
 *
 * It manages:
 *  - All customers          â†’ Map<String, Customer>       (customerId â†’ Customer)
 *  - Transaction history    â†’ List<Transaction>
 *  - Processed ref IDs      â†’ Set<String>                 (for duplicate detection)
 *  - Failed transactions    â†’ List<Transaction>
 *
 * WHY these collections?
 *  - Map  : fast lookup by key (find a customer by ID instantly)
 *  - List : ordered history (keeps insertion order, allows duplicates)
 *  - Set  : NO duplicates allowed â€” perfect for tracking used reference IDs
 */
public class PaymentService {

    // Map: key = customerId, value = Customer object
    private Map<String, Customer> customers = new HashMap<>();

    // List: every transaction ever attempted (success + failed + duplicate)
    private List<Transaction> transactionHistory = new ArrayList<>();

    // Set: stores referenceIds that have already been processed successfully
    // A Set automatically rejects duplicates â€” that's exactly what we need!
    private Set<String> processedReferenceIds = new HashSet<>();

    // List: only the transactions that FAILED (for reporting/debugging)
    private List<Transaction> failedTransactions = new ArrayList<>();

    /** Register a new customer in the system. */
    public void registerCustomer(Customer customer) {
        customers.put(customer.getCustomerId(), customer);
        System.out.println("[PaymentService] Registered customer: " + customer.getFullName());
    }

    /** Look up a customer by their ID. */
    public Customer findCustomer(String customerId) {
        return customers.get(customerId); // returns null if not found
    }

    /** Get all customers as a list. */
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    /**
     * This is the most important method.
     *
     * Steps:
     *  1. Check if the referenceId has been used before â†’ DUPLICATE? Reject it.
     *  2. Find the customer and their account.
     *  3. Let the account process the transaction (polymorphism in action).
     *  4. Record everything.
     *
     * @param customerId  who is making the transaction
     * @param accountId   which account to use
     * @param transaction the transaction to process
     */
    public void processTransaction(String customerId, String accountId, Transaction transaction) {

        System.out.println("\nâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€");
        System.out.println("[PaymentService] New request | Ref: " + transaction.getReferenceId());
        // Set.contains() checks if this referenceId was already processed
        if (processedReferenceIds.contains(transaction.getReferenceId())) {
            System.out.println("[PaymentService] âš  DUPLICATE DETECTED! Ref '"
                    + transaction.getReferenceId() + "' was already processed. Rejecting.");
            transaction.setStatus(Transaction.STATUS_DUPLICATE);
            transactionHistory.add(transaction);   // still log it
            failedTransactions.add(transaction);   // count as failed
            return; // stop here â€” do NOT process again
        }
        Customer customer = findCustomer(customerId);
        if (customer == null) {
            System.out.println("[PaymentService] âœ— Customer not found: " + customerId);
            transaction.setStatus(Transaction.STATUS_FAILED);
            transactionHistory.add(transaction);
            failedTransactions.add(transaction);
            return;
        }
        Account account = customer.getAccountById(accountId);
        if (account == null) {
            System.out.println("[PaymentService] âœ— Account not found: " + accountId);
            transaction.setStatus(Transaction.STATUS_FAILED);
            transactionHistory.add(transaction);
            failedTransactions.add(transaction);
            return;
        }
        // account.processTransaction() calls the correct version:
        //   â†’ WalletAccount.processTransaction()  OR
        //   â†’ SavingsAccount.processTransaction()
        // Java decides which one at RUNTIME based on the actual object type.
        account.processTransaction(transaction);
        // Set.add() returns false if the value already exists â€” but we already
        // checked above, so this will always succeed here.
        processedReferenceIds.add(transaction.getReferenceId());
        transactionHistory.add(transaction);
        System.out.println("[PaymentService] âœ“ Transaction recorded. Status: " + transaction.getStatus());
    }

    /** Print all transactions in history. */
    public void printTransactionHistory() {
        System.out.println("\nâ•â•â•â•â•â•â•â•â•â• TRANSACTION HISTORY â•â•â•â•â•â•â•â•â•â•");
        if (transactionHistory.isEmpty()) {
            System.out.println("  (no transactions yet)");
        }
        for (Transaction t : transactionHistory) {
            System.out.println("  " + t);
        }
        System.out.println("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
    }

    /** Print only failed/duplicate transactions. */
    public void printFailedTransactions() {
        System.out.println("\nâ•â•â•â•â•â•â•â•â•â• FAILED / DUPLICATE TRANSACTIONS â•â•â•â•â•â•â•â•â•â•");
        if (failedTransactions.isEmpty()) {
            System.out.println("  (none)");
        }
        for (Transaction t : failedTransactions) {
            System.out.println("  " + t);
        }
        System.out.println("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
    }

    /** Print all processed reference IDs (the duplicate-prevention Set). */
    public void printProcessedReferenceIds() {
        System.out.println("\nâ•â•â•â•â•â•â•â•â•â• PROCESSED REFERENCE IDs (Set) â•â•â•â•â•â•â•â•â•â•");
        System.out.println("  " + processedReferenceIds);
        System.out.println("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
    }
    public List<Transaction> getTransactionHistory()    { return transactionHistory; }
    public List<Transaction> getFailedTransactions()    { return failedTransactions; }
    public Set<String>       getProcessedReferenceIds() { return processedReferenceIds; }
    public Map<String, Customer> getCustomers()         { return customers; }
}
