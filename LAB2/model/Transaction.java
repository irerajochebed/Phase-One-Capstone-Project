package com.igirepay.igirepaypaymentgateway.LAB2.model;

import java.time.LocalDateTime;

public class Transaction {
    public static final String TYPE_DEPOSIT    = "DEPOSIT";
    public static final String TYPE_WITHDRAWAL = "WITHDRAWAL";
    public static final String TYPE_TRANSFER   = "TRANSFER";
    public static final String STATUS_SUCCESS   = "SUCCESS";
    public static final String STATUS_FAILED    = "FAILED";
    public static final String STATUS_DUPLICATE = "DUPLICATE";

    private int           id;               
    private int           accountId;        
    private String        referenceId;      
    private String        transactionType;  
    private double        amount;           
    private String        status;           
    private String        description;      
    private LocalDateTime createdAt;       
    public Transaction(int accountId, String referenceId,
                       String transactionType, double amount, String description) {
        this.accountId       = accountId;
        this.referenceId     = referenceId;
        this.transactionType = transactionType;
        this.amount          = amount;
        this.description     = description;
        this.status          = STATUS_SUCCESS; 
        
    }
    public Transaction(int id, int accountId, String referenceId,
                       String transactionType, double amount,
                       String status, String description, LocalDateTime createdAt) {
        this.id              = id;
        this.accountId       = accountId;
        this.referenceId     = referenceId;
        this.transactionType = transactionType;
        this.amount          = amount;
        this.status          = status;
        this.description     = description;
        this.createdAt       = createdAt;
    }
    public int    getId()              {
         return id; }
    public void   setId(int id)        {
         this.id = id; }

    public int    getAccountId()                 {
         return accountId; }
    public void   setAccountId(int accountId)    {
         this.accountId = accountId; }

    public String getReferenceId()                     {
         return referenceId; }
    public void   setReferenceId(String referenceId)   {
         this.referenceId = referenceId; }

    public String getTransactionType()                         {
         return transactionType; }
    public void   setTransactionType(String transactionType)   {
         this.transactionType = transactionType; }

    public double getAmount()              {
         return amount; }
    public void   setAmount(double amount) {
         this.amount = amount; }

    public String getStatus()                {
         return status; }
    public void   setStatus(String status)   {
         this.status = status; }

    public String getDescription()                     {
         return description; }
    public void   setDescription(String description)   {
         this.description = description; }

    public LocalDateTime getCreatedAt()                 {
         return createdAt; }
    public void          setCreatedAt(LocalDateTime dt) {
         this.createdAt = dt; }

    @Override
    public String toString() {
        return "Transaction{id=" + id +
                ", accountId="  + accountId       +
                ", ref='"       + referenceId      + '\'' +
                ", type='"      + transactionType  + '\'' +
                ", amount="     + amount            +
                ", status='"    + status            + '\'' +
                ", desc='"      + description       + '\'' +
                '}';
    }
}
