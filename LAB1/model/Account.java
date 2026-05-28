package com.igirepay.igirepaypaymentgateway.LAB1.model;


public abstract class Account {


    private String accountId;
    private String accountNumber;
    private double balance;
    private String currency;

    public Account(String accountId, String accountNumber, double balance, String currency) {
        this.accountId     = accountId;
        this.accountNumber = accountNumber;
        this.balance       = balance;
        this.currency      = currency;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public abstract void deposit(double amount);


    public abstract void withdraw(double amount);


    public abstract void processTransaction(Transaction transaction);


    @Override
    public String toString() {
        return "Account{" +
                "accountId='"     + accountId     + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", balance="      + balance        +
                ", currency='"    + currency       + '\'' +
                '}';
    }
}
