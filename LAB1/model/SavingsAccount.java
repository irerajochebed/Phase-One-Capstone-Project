package com.igirepay.igirepaypaymentgateway.LAB1.model;

public class SavingsAccount extends Account {


    private double withdrawalLimit;
    private double withdrawalFeePercent;
    private int    withdrawalCount;


    public SavingsAccount(String accountId, String accountNumber,
                          double balance, String currency,
                          double withdrawalLimit, double withdrawalFeePercent) {
        super(accountId, accountNumber, balance, currency);
        this.withdrawalLimit      = withdrawalLimit;
        this.withdrawalFeePercent = withdrawalFeePercent;
        this.withdrawalCount      = 0;
    }


    public double getWithdrawalLimit()                         { return withdrawalLimit; }
    public void   setWithdrawalLimit(double withdrawalLimit)   { this.withdrawalLimit = withdrawalLimit; }

    public double getWithdrawalFeePercent()                              { return withdrawalFeePercent; }
    public void   setWithdrawalFeePercent(double withdrawalFeePercent)   { this.withdrawalFeePercent = withdrawalFeePercent; }

    public int getWithdrawalCount() { return withdrawalCount; }

        @Override
    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("[SavingsAccount] Deposit failed: amount must be positive.");
            return;
        }
        setBalance(getBalance() + amount);
        System.out.println("[SavingsAccount] Deposited " + amount + " " + getCurrency()
                + " | New balance: " + getBalance());
    }

    /**
     * Savings withdrawal: checks the withdrawal LIMIT and deducts a FEE.
     */
    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("[SavingsAccount] Withdrawal failed: amount must be positive.");
            return;
        }
        if (amount > withdrawalLimit) {
            System.out.println("[SavingsAccount] Withdrawal failed: exceeds limit of "
                    + withdrawalLimit + " " + getCurrency());
            return;
        }

        // Calculate the fee
        double fee        = amount * (withdrawalFeePercent / 100.0);
        double totalCost  = amount + fee; // total deducted from balance

        if (totalCost > getBalance()) {
            System.out.println("[SavingsAccount] Withdrawal failed: insufficient funds. "
                    + "Need " + totalCost + " (incl. fee), have " + getBalance());
            return;
        }

        setBalance(getBalance() - totalCost);
        withdrawalCount++;
        System.out.println("[SavingsAccount] Withdrew " + amount + " + fee " + fee
                + " = " + totalCost + " " + getCurrency()
                + " | New balance: " + getBalance());
    }

        @Override
    public void processTransaction(Transaction transaction) {
        System.out.println("[SavingsAccount] Processing: " + transaction.getTransactionType()
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
                System.out.println("[SavingsAccount] Unknown transaction type.");
                transaction.setStatus(Transaction.STATUS_FAILED);
        }
    }
    @Override
    public String toString() {
        return "SavingsAccount{" +
                "accountId='"       + getAccountId()       + '\'' +
                ", accountNumber='"  + getAccountNumber()   + '\'' +
                ", balance="         + getBalance()          +
                ", currency='"       + getCurrency()         + '\'' +
                ", withdrawalLimit=" + withdrawalLimit        +
                ", feePercent="      + withdrawalFeePercent   +
                ", withdrawalCount=" + withdrawalCount        +
                '}';
    }
}
