package com.igirepay.igirepay.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaymentManager {

    // -----------------------------------------------
    // COLLECTIONS
    // -----------------------------------------------

    // Map — customerId → Account (fast lookup)
    private Map<String, Account> customerAccounts;

    // List — all successful transactions in order
    private List<Transaction> transactionHistory;

    // Set — referenceIds already processed (duplicate shield)
    private Set<String> processedReferenceIds;

    // List — all failed/rejected transactions
    private List<Transaction> failedTransactionLog;

    // -----------------------------------------------
    // Constructor
    // -----------------------------------------------
    public PaymentManager() {
        this.customerAccounts      = new HashMap<>();
        this.transactionHistory    = new ArrayList<>();
        this.processedReferenceIds = new HashSet<>();
        this.failedTransactionLog  = new ArrayList<>();
    }

    // -----------------------------------------------
    // ACCOUNT MANAGEMENT
    // -----------------------------------------------

    public void addAccount(String customerId, Account account) {
        customerAccounts.put(customerId, account);
        System.out.println("✅ Account registered for customer: " + customerId);
    }

    public Account getAccount(String customerId) {
        return customerAccounts.get(customerId);
    }

    public void removeAccount(String customerId) {
        if (customerAccounts.containsKey(customerId)) {
            customerAccounts.remove(customerId);
            System.out.println("✅ Account removed for customer: " + customerId);
        } else {
            System.out.println("❌ No account found for customer: " + customerId);
        }
    }

    public void displayAllAccounts() {
        System.out.println("\n===== ALL REGISTERED ACCOUNTS =====");
        if (customerAccounts.isEmpty()) {
            System.out.println("No accounts registered.");
            return;
        }
        for (Map.Entry<String, Account> entry : customerAccounts.entrySet()) {
            System.out.println("Customer ID : " + entry.getKey());
            System.out.println("Account     : " + entry.getValue());
            System.out.println("------------------------------------");
        }
    }

    // -----------------------------------------------
    // DUPLICATE DETECTION
    // -----------------------------------------------

    public boolean isDuplicate(String referenceId) {
        return processedReferenceIds.contains(referenceId);
    }

    // -----------------------------------------------
    // TRANSACTION PROCESSING
    // -----------------------------------------------

    public String processTransaction(String customerId,
                                     Transaction transaction) {

        // STEP 1 — duplicate check using Set
        if (isDuplicate(transaction.getReferenceId())) {
            failedTransactionLog.add(transaction);
            String msg = "❌ DUPLICATE REJECTED! Ref: "
                    + transaction.getReferenceId()
                    + " was already processed.";
            System.out.println(msg);
            return msg;
        }

        // STEP 2 — find account in Map
        Account account = customerAccounts.get(customerId);
        if (account == null) {
            failedTransactionLog.add(transaction);
            String msg = "❌ Transaction failed: No account found for customer: "
                    + customerId;
            System.out.println(msg);
            return msg;
        }

        // STEP 3 — process using polymorphism
        String result = account.processTransaction(transaction);

        // STEP 4 — mark referenceId as processed in Set
        processedReferenceIds.add(transaction.getReferenceId());

        // STEP 5 — add to history List
        transactionHistory.add(transaction);

        return result;
    }

    // -----------------------------------------------
    // DISPLAY REPORTS
    // -----------------------------------------------

    public void displayTransactionHistory() {
        System.out.println("\n===== TRANSACTION HISTORY =====");
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        for (Transaction t : transactionHistory) {
            System.out.println(t);
        }
    }

    public void displayFailedTransactions() {
        System.out.println("\n===== FAILED TRANSACTION LOG =====");
        if (failedTransactionLog.isEmpty()) {
            System.out.println("No failed transactions.");
            return;
        }
        for (Transaction t : failedTransactionLog) {
            System.out.println("FAILED → " + t);
        }
    }

    public void displayProcessedReferenceIds() {
        System.out.println("\n===== PROCESSED REFERENCE IDs =====");
        if (processedReferenceIds.isEmpty()) {
            System.out.println("None yet.");
            return;
        }
        for (String refId : processedReferenceIds) {
            System.out.println("✅ " + refId);
        }
    }

    // -----------------------------------------------
    // GETTERS for collections
    // -----------------------------------------------

    public List<Transaction> getTransactionHistory()   { return transactionHistory; }
    public List<Transaction> getFailedTransactionLog() { return failedTransactionLog; }
    public Set<String> getProcessedReferenceIds()      { return processedReferenceIds; }
    public Map<String, Account> getCustomerAccounts()  { return customerAccounts; }
}