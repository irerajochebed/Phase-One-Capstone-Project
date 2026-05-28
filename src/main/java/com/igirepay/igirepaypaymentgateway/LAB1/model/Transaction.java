package com.igirepay.igirepaypaymentgateway.LAB1.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Transaction represents a single financial operation.
 *
 * Key field: referenceId  this is the UNIQUE ID sent by the client.
 * If the same referenceId comes in twice, we know it's a DUPLICATE and reject it.
 * This concept is called IDEMPOTENCY (same request = same result, processed only once).
 */
public class Transaction {

    //  Transaction Types (like an enum but kept simple) 
    public static final String TYPE_DEPOSIT    = "DEPOSIT";
    public static final String TYPE_WITHDRAWAL = "WITHDRAWAL";
    public static final String TYPE_TRANSFER   = "TRANSFER";

    //  Transaction Status 
    public static final String STATUS_SUCCESS  = "SUCCESS";
    public static final String STATUS_FAILED   = "FAILED";
    public static final String STATUS_DUPLICATE = "DUPLICATE";

    //  Fields 
    private String        transactionId;   // internal ID we generate e.g. "TXN001"
    private String        referenceId;     // ID sent by the CLIENT (used for duplicate check)
    private double        amount;          // how much money
    private String        transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER
    private LocalDateTime timestamp;       // when did this happen
    private String        status;          // SUCCESS, FAILED, DUPLICATE
    private String        description;     // optional note e.g. "Payment for order #55"

    //  Constructor 
    public Transaction(String transactionId, String referenceId,
                       double amount, String transactionType, String description) {
        this.transactionId   = transactionId;
        this.referenceId     = referenceId;
        this.amount          = amount;
        this.transactionType = transactionType;
        this.description     = description;
        this.timestamp       = LocalDateTime.now(); // automatically set to right now
        this.status          = STATUS_SUCCESS;       // default to success; can be changed
    }

    //  Getters & Setters 
    public String getTransactionId()                       { return transactionId; }
    public void   setTransactionId(String transactionId)   { this.transactionId = transactionId; }

    public String getReferenceId()                     { return referenceId; }
    public void   setReferenceId(String referenceId)   { this.referenceId = referenceId; }

    public double getAmount()              { return amount; }
    public void   setAmount(double amount) { this.amount = amount; }

    public String getTransactionType()                         { return transactionType; }
    public void   setTransactionType(String transactionType)   { this.transactionType = transactionType; }

    public LocalDateTime getTimestamp()                    { return timestamp; }
    public void          setTimestamp(LocalDateTime ts)    { this.timestamp = ts; }

    public String getStatus()                { return status; }
    public void   setStatus(String status)   { this.status = status; }

    public String getDescription()                     { return description; }
    public void   setDescription(String description)   { this.description = description; }

    //  toString 
    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "Transaction{" +
                "transactionId='" + transactionId   + '\'' +
                ", referenceId='" + referenceId     + '\'' +
                ", amount="       + amount           +
                ", type='"        + transactionType  + '\'' +
                ", status='"      + status           + '\'' +
                ", time='"        + timestamp.format(fmt) + '\'' +
                ", desc='"        + description      + '\'' +
                '}';
    }
}
