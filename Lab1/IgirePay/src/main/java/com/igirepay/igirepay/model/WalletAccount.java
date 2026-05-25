package com.igirepay.igirepay.model;

/**
 * WalletAccount class for instant money transfers.
 * Allows quick transactions with daily limits and PIN protection.
 */
public class WalletAccount extends Account {

    // Fields
    private String phoneNumber;
    private String pin;
    private boolean isActive;
    private double dailyTransferLimit;  // Daily transfer limit for security
    private double dailyTransferTotal;  // Track daily transfers

    // Default Constructor
    public WalletAccount() {
        super();
        this.isActive = true;
        this.dailyTransferLimit = 1000000.00;  // 1,000,000 RWF default daily limit
        this.dailyTransferTotal = 0.0;
    }

    // Full Constructor
    public WalletAccount(String accountId, double balance,
                         String phoneNumber, String pin) {
        super(accountId, balance, "WALLET");
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.isActive = true;
        this.dailyTransferLimit = 1000000.00;  // 1,000,000 RWF daily limit
        this.dailyTransferTotal = 0.0;
    }

    // Complete Constructor
    public WalletAccount(String accountId, double balance, String phoneNumber,
                         String pin, boolean isActive, double dailyLimit) {
        super(accountId, balance, "WALLET");
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.isActive = isActive;
        this.dailyTransferLimit = dailyLimit;
        this.dailyTransferTotal = 0.0;
    }

    // Getters
    public String getPhoneNumber()      { return phoneNumber; }
    public String getPin()              { return pin; }
    public boolean isActive()           { return isActive; }
    public double getDailyTransferLimit() { return dailyTransferLimit; }

    // Setters
    public void setPin(String pin)                       { this.pin = pin; }
    public void setActive(boolean isActive)              { this.isActive = isActive; }
    public void setPhoneNumber(String phone)             { this.phoneNumber = phone; }
    public void setDailyTransferLimit(double limit)      { this.dailyTransferLimit = limit; }

    // -----------------------------------------------
    // POLYMORPHISM — overriding parent abstract methods
    // -----------------------------------------------

    @Override
    public boolean withdraw(double amount) {

        // Rule 1 — account must be active
        if (!isActive) {
            System.out.println("❌ Withdrawal failed: Wallet is inactive.");
            return false;
        }

        // Rule 2 — amount must be greater than zero
        if (amount <= 0) {
            System.out.println("❌ Withdrawal failed: Amount must be greater than 0.");
            return false;
        }

        // Rule 3 — must have enough balance
        if (getBalance() < amount) {
            System.out.println("❌ Withdrawal failed: Insufficient balance." +
                    " Available: " + getBalance());
            return false;
        }

        // All checks passed — deduct and confirm
        setBalance(getBalance() - amount);
        System.out.println("✅ Wallet withdrawal successful! Amount: " + amount +
                " | New balance: " + getBalance());
        return true;
    }

    @Override
    public boolean deposit(double amount) {

        // Rule 1 — amount must be greater than zero
        if (amount <= 0) {
            System.out.println("❌ Deposit failed: Amount must be greater than 0.");
            return false;
        }

        // All checks passed — add to balance
        setBalance(getBalance() + amount);
        System.out.println("✅ Wallet deposit successful! Amount: " + amount +
                " | New balance: " + getBalance());
        return true;
    }

    @Override
    public String processTransaction(Transaction transaction) {

        // Rule 1 — wallet must be active
        if (!isActive) {
            return "❌ Transaction failed: Wallet is inactive.";
        }

        // Rule 2 — check transaction type and process accordingly
        if (transaction.getTransactionType() == Transaction.TransactionType.SEND) {
            boolean success = withdraw(transaction.getAmount());
            if (success) {
                return "✅ SEND transaction processed instantly. Ref: "
                        + transaction.getReferenceId();
            } else {
                return "❌ SEND transaction failed. Ref: "
                        + transaction.getReferenceId();
            }
        }

        if (transaction.getTransactionType() == Transaction.TransactionType.RECEIVE) {
            boolean success = deposit(transaction.getAmount());
            if (success) {
                return "✅ RECEIVE transaction processed. Ref: "
                        + transaction.getReferenceId();
            } else {
                return "❌ RECEIVE transaction failed. Ref: "
                        + transaction.getReferenceId();
            }
        }

        return "⚠️ Unknown transaction type.";
    }

    // toString
    @Override
    public String toString() {
        return "WalletAccount{" +
                "accountId='"    + getAccountId()  + '\'' +
                ", balance="     + getBalance()    +
                ", phoneNumber='" + phoneNumber    + '\'' +
                ", isActive="    + isActive        +
                '}';
    }
}