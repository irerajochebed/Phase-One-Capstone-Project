package com.igirepay.igirepaypaymentgateway.LAB1.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Transaction {
    public static final String TYPE_DEPOSIT    = "DEPOSIT";
    public static final String TYPE_WITHDRAWAL = "WITHDRAWAL";
    public static final String TYPE_TRANSFER   = "TRANSFER";
    public static final String STATUS_SUCCESS  = "SUCCESS";
    public static final String STATUS_FAILED   = "FAILED";
    public static final String STATUS_DUPLICATE = "DUPLICATE";
    private String        transactionId;   
    private String        referenceId;    
    private double        amount;          
    private String        transactionType; 
    private LocalDateTime timestamp;       
    private String        status;          
    private String        description;     
    public Transaction(String transactionId, String referenceId,
                       double amount, String transactionType, String description) {
        this.transactionId   = transactionId;
        this.referenceId     = referenceId;
        this.amount          = amount;
        this.transactionType = transactionType;
        this.description     = description;
        this.timestamp       = LocalDateTime.now(); 
        this.status          = STATUS_SUCCESS;      
            }


    public String getTransactionId(){
         return transactionId; 
    }
    public void   setTransactionId(String transactionId)   {
         this.transactionId = transactionId; 
    }

    public String getReferenceId(){
         return referenceId; 
    }
    public void   setReferenceId(String referenceId)   {
         this.referenceId = referenceId; 
    

    public double getAmount(){
         return amount; 
    }
    public void   setAmount(double amount) {
         this.amount = amount; 
    }

    public String getTransactionType() { 
        return transactionType;
     }
    public void   setTransactionType(String transactionType)   {
         this.transactionType = transactionType;
     }

    public LocalDateTime getTimestamp(){
         return timestamp; 
    }
    public void          setTimestamp(LocalDateTime ts)    {
         this.timestamp = ts; 
    }

    public String getStatus(){
         return status;
     }
    public void   setStatus(String status)   {
         this.status = status; 
    }

    public String getDescription(){ 
        return description; 
    }
    public void   setDescription(String description)   {
         this.description = description;
     }
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
