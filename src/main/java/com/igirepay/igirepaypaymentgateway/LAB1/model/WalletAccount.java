package com.igirepay.igirepaypaymentgateway.LAB1.model;

import com.igirepay.igirepaypaymentgateway.LAB1.model.Transaction;

public class WalletAccount extends Account {

    private String phoneNumber;
    private boolean isActive;

    public WalletAccount(String accountId, String accountNumber,
                         double balance, String currency, String phoneNumber) {
        super(accountId, accountNumber, balance, currency);
        this.phoneNumber = phoneNumber;
        this.isActive = true;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("[WalletAccount] Deposit failed: amount must be positive.");
            return;
        }
        setBalance(getBalance() + amount);
        System.out.println("[WalletAccount] Deposited " + amount + " " + getCurrency()
                + " | New balance: " + getBalance());
    }

    @Override
    public void withdraw(double amount) {
        if (!isActive) {
            System.out.println("[WalletAccount] Withdrawal failed: wallet is inactive.");
            return;
        }
        if (amount <= 0) {
            System.out.println("[WalletAccount] Withdrawal failed: amount must be positive.");
            return;
        }
        if (amount > getBalance()) {
            System.out.println("[WalletAccount] Withdrawal failed: insufficient funds. "
                    + "Balance=" + getBalance() + ", Requested=" + amount);
            return;
        }
        setBalance(getBalance() - amount);
        System.out.println("[WalletAccount] Withdrew " + amount + " " + getCurrency()
                + " | New balance: " + getBalance());
    }

    @Override
    public void processTransaction(Transaction transaction) {
        System.out.println("[WalletAccount] Processing: " + transaction.getTransactionType()
                + " | Ref: " + transaction.getReferenceId());

        switch (transaction.getTransactionType()) {
            case Transaction.TYPE_DEPOSIT:
                deposit(transaction.getAmount());
                break;
            case Transaction.TYPE_WITHDRAWAL:
            case Transaction.TYPE_TRANSFER:
                withdraw(transaction.getAmount());
                break;
            default:
                System.out.println("[WalletAccount] Unknown transaction type.");
                transaction.setStatus(Transaction.STATUS_FAILED);
        }
    }

    @Override
    public String toString() {
        return "WalletAccount{" +
                "accountId='" + getAccountId() + '\'' +
                ", accountNumber='" + getAccountNumber() + '\'' +
                ", balance=" + getBalance() +
                ", currency='" + getCurrency() + '\'' +
                ", phone='" + phoneNumber + '\'' +
                ", active=" + isActive +
                '}';
    }
}
