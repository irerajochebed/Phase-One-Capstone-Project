package com.igirepay.igirepay.model;

import java.time.LocalDateTime;

/**
 * Abstract base class for all account types in IgirePay.
 * Defines common properties and abstract methods for account operations.
 */
public abstract class Account {
    private String accountId;
    private double balance;
    private String accountType;
    private LocalDateTime createdAt;

    // Default Constructor
    public Account() {
        this.createdAt = LocalDateTime.now();
    }

    // Full Constructor
    public Account(String accountId, double balance, String accountType) {
        this.accountId = accountId;
        this.balance = balance;
        this.accountType = accountType;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with all parameters
    public Account(String accountId, double balance, String accountType, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.balance = balance;
        this.accountType = accountType;
        this.createdAt = createdAt;
    }

    // ========== GETTERS AND SETTERS ==========
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        if (balance < 0) {
            System.out.println("⚠️ Warning: Balance cannot be negative!");
            return;
        }
        this.balance = balance;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ========== ABSTRACT METHODS ==========
    // These must be implemented by subclasses

    /**
     * Withdraw money from account.
     * @param amount The amount to withdraw
     * @return true if successful, false otherwise
     */
    public abstract boolean withdraw(double amount);

    /**
     * Deposit money into account.
     * @param amount The amount to deposit
     * @return true if successful, false otherwise
     */
    public abstract boolean deposit(double amount);

    /**
     * Process a complete transaction object.
     * Different account types may handle transactions differently.
     * @param transaction The transaction to process
     * @return A message describing the result
     */
    public abstract String processTransaction(Transaction transaction);

    @Override
    public String toString() {
        return "Account{" +
                "accountId='" + accountId + '\'' +
                ", balance=" + balance +
                ", accountType='" + accountType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
