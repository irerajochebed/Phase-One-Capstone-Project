package com.igirepay.igirepaypaymentgateway.LAB2.model;

import java.time.LocalDateTime;


public class Account {

    private int           id;           
    private int           customerId;   
    private String        accountType;  
    private double        balance;      
    private String        currency;     
    private boolean       isActive;    
    private LocalDateTime lastTransactionDate; 
    private LocalDateTime createdAt;    
    public Account(int customerId, String accountType, double balance, String currency) {
        this.customerId  = customerId;
        this.accountType = accountType;
        this.balance     = balance;
        this.currency    = currency;
        this.isActive    = true;  

    }
    public Account(int id, int customerId, String accountType,
                   double balance, String currency, boolean isActive,
                   LocalDateTime lastTransactionDate, LocalDateTime createdAt) {
        this.id          = id;
        this.customerId  = customerId;
        this.accountType = accountType;
        this.balance     = balance;
        this.currency    = currency;
        this.isActive    = isActive;
        this.lastTransactionDate = lastTransactionDate;
        this.createdAt   = createdAt;
    }
    public int    getId{
         return id; 
    }
    public void   setId(int id{
         this.id = id; 
    }

    public int    getCustomerId(){
         return customerId;
     }
    public void   setCustomerId(int customerId){
         this.customerId = customerId;


    public String getAccountType() {
         return accountType; 
    }
    public void   setAccountType(String accountType){
         this.accountType = accountType; 
    }

    public double getBalance(){ 
        return balance; 
    }
    public void   setBalance(double balance) { 
        this.balance = balance;
     }

    public String getCurrency()                  {
         return currency; 
    }
    public void   setCurrency(String currency)   {
         this.currency = currency; 
    }

    public boolean isActive()                { 
        return isActive; 
    }
    public void    setActive(boolean active) { 
        this.isActive = active; 
    }

    public LocalDateTime getLastTransactionDate()                 {
         return lastTransactionDate; 
    }
    public void          setLastTransactionDate(LocalDateTime dt) {
         this.lastTransactionDate = dt;
     }

    public LocalDateTime getCreatedAt()                 {
         return createdAt; 
    }
    public void          setCreatedAt(LocalDateTime dt) {
         this.createdAt = dt; 
    }

    @Override
    public String toString() {
        return "Account{id=" + id +
                ", customerId=" + customerId  +
                ", type='"     + accountType  + '\'' +
                ", balance="   + balance       +
                ", currency='" + currency      + '\'' +
                '}';
    }
}
