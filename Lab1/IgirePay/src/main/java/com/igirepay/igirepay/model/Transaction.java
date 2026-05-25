package com.igirepay.igirepay.model;

import java.time.LocalDateTime;

/**
 * Transaction class to represent financial transactions in IgirePay.
 * Each transaction has a unique transaction ID and reference ID for duplicate detection.
 */
public class Transaction {

    /**
     * Enum for different types of transactions supported by IgirePay.
     */
    public enum TransactionType {
        SEND,           // Send money to another account
        RECEIVE,        // Receive money from another account
        DEPOSIT,        // Deposit into savings
        WITHDRAWAL,     // Withdraw from savings
        LOAN_REPAYMENT  // Repay a loan
    }

    // Fields
    private String transactionId;      // Unique transaction identifier
    private String referenceId;        // Reference ID for duplicate detection (idempotency key)
    private double amount;             // Transaction amount
    private TransactionType transactionType;  // Type of transaction
    private LocalDateTime timestamp;   // When the transaction occurred
    private String senderAccountId;    // Account sending the money
    private String receiverAccountId;  // Account receiving the money

    // Constructors

    /**
     * Default Constructor
     */
    public Transaction() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Full Constructor with all parameters
     */
    public Transaction(String transactionId, String referenceId, double amount,
                       TransactionType transactionType, LocalDateTime timestamp,
                       String senderAccountId, String receiverAccountId) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = timestamp;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
    }

    /**
     * Constructor for simple transactions
     */
    public Transaction(String transactionId, String referenceId, double amount,
                       TransactionType transactionType) {
        this.transactionId = transactionId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();
    }

    // ========== GETTERS AND SETTERS ==========

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderAccountId() {
        return senderAccountId;
    }

    public void setSenderAccountId(String senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public String getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(String receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", amount=" + amount +
                ", transactionType=" + transactionType +
                ", timestamp=" + timestamp +
                ", senderAccountId='" + senderAccountId + '\'' +
                ", receiverAccountId='" + receiverAccountId + '\'' +
                '}';
    }
}
