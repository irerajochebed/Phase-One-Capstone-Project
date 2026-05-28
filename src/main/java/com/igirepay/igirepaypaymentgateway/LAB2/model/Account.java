package com.igirepay.igirepaypaymentgateway.LAB2.model;

import java.time.LocalDateTime;

/**
 * Account model  mirrors the 'accounts' table.
 *
 * accountType is either "WALLET" or "SAVINGS".
 */
public class Account {

    private int           id;           // maps to: id
    private int           customerId;   // maps to: customer_id (foreign key)
    private String        accountType;  // maps to: account_type  ("WALLET" or "SAVINGS")
    private double        balance;      // maps to: balance
    private String        currency;     // maps to: currency  (e.g. "RWF")
    private boolean       isActive;     // maps to: is_active (true = active, false = inactive)
    private LocalDateTime lastTransactionDate; // maps to: last_transaction_date
    private LocalDateTime createdAt;    // maps to: created_at

    //  Constructor for creating a NEW account 
    public Account(int customerId, String accountType, double balance, String currency) {
        this.customerId  = customerId;
        this.accountType = accountType;
        this.balance     = balance;
        this.currency    = currency;
        this.isActive    = true;  // new accounts are active by default
    }

    //  Constructor for reading FROM the database 
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

    //  Getters & Setters 
    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public int    getCustomerId()                  { return customerId; }
    public void   setCustomerId(int customerId)    { this.customerId = customerId; }

    public String getAccountType()                     { return accountType; }
    public void   setAccountType(String accountType)   { this.accountType = accountType; }

    public double getBalance()               { return balance; }
    public void   setBalance(double balance) { this.balance = balance; }

    public String getCurrency()                  { return currency; }
    public void   setCurrency(String currency)   { this.currency = currency; }

    public boolean isActive()                { return isActive; }
    public void    setActive(boolean active) { this.isActive = active; }

    public LocalDateTime getLastTransactionDate()                 { return lastTransactionDate; }
    public void          setLastTransactionDate(LocalDateTime dt) { this.lastTransactionDate = dt; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime dt) { this.createdAt = dt; }

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
