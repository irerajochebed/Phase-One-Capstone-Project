package com.igirepay.igirepay.model;

import java.time.LocalDate;

/**
 * SavingsAccount class for long-term savings with interest.
 * Features withdrawal limits (3 per month) and 2% withdrawal fees.
 */
public class SavingsAccount extends Account {

    // Fields
    private double interestRate;
    private double targetAmount;
    private LocalDate maturityDate;

    // Withdrawal tracking — max 3 per month allowed
    private int withdrawalCount;
    private static final int MAX_WITHDRAWALS = 3;

    // Fee applied on every withdrawal — 2%
    private static final double WITHDRAWAL_FEE_PERCENT = 0.02;

    // Default Constructor
    public SavingsAccount() {
        super("SAVINGS-" + System.currentTimeMillis(), 0.0, "SAVINGS");
        this.interestRate = 0.05;  // 5% default
        this.targetAmount = 100000.0;
        this.maturityDate = LocalDate.now().plusYears(1);
        this.withdrawalCount = 0;
    }

    // Full Constructor
    public SavingsAccount(String accountId, double balance,
                          double interestRate, double targetAmount,
                          LocalDate maturityDate) {
        super(accountId, balance, "SAVINGS");
        this.interestRate = interestRate;
        this.targetAmount = targetAmount;
        this.maturityDate = maturityDate;
        this.withdrawalCount = 0;  // Starts at zero
    }

    // Getters
    public double getInterestRate()    { return interestRate; }
    public double getTargetAmount()    { return targetAmount; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public int getWithdrawalCount()    { return withdrawalCount; }

    // Setters
    public void setInterestRate(double interestRate)    { this.interestRate = interestRate; }
    public void setTargetAmount(double targetAmount)    { this.targetAmount = targetAmount; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }

    // -----------------------------------------------
    // POLYMORPHISM — overriding parent abstract methods
    // -----------------------------------------------

    @Override
    public boolean withdraw(double amount) {

        // Rule 1 — amount must be greater than zero
        if (amount <= 0) {
            System.out.println("❌ Withdrawal failed: Amount must be greater than 0.");
            return false;
        }

        // Rule 2 — check withdrawal limit (max 3 per month)
        if (withdrawalCount >= MAX_WITHDRAWALS) {
            System.out.println("❌ Withdrawal failed: Monthly limit of "
                    + MAX_WITHDRAWALS + " withdrawals reached.");
            return false;
        }

        // Rule 3 — calculate fee and total deduction
        double fee          = amount * WITHDRAWAL_FEE_PERCENT;
        double totalDeducted = amount + fee;

        // Rule 4 — must have enough balance to cover amount + fee
        if (getBalance() < totalDeducted) {
            System.out.println("❌ Withdrawal failed: Insufficient balance." +
                    " Amount + Fee = " + totalDeducted +
                    " | Available: " + getBalance());
            return false;
        }

        // All checks passed — deduct, count, confirm
        setBalance(getBalance() - totalDeducted);
        withdrawalCount++;

        System.out.println("✅ Savings withdrawal successful!" +
                " Amount: "  + amount +
                " | Fee: "   + fee +
                " | Total deducted: " + totalDeducted +
                " | New balance: "    + getBalance() +
                " | Withdrawals this month: " + withdrawalCount
                + "/" + MAX_WITHDRAWALS);
        return true;
    }

    @Override
    public boolean deposit(double amount) {

        // Rule 1 — amount must be greater than zero
        if (amount <= 0) {
            System.out.println("❌ Deposit failed: Amount must be greater than 0.");
            return false;
        }

        // No fee on deposits — just add the amount
        setBalance(getBalance() + amount);
        System.out.println("✅ Savings deposit successful!" +
                " Amount: " + amount +
                " | New balance: " + getBalance() +
                " | Target: " + targetAmount);
        return true;
    }

    @Override
    public String processTransaction(Transaction transaction) {

        // Savings accounts only support DEPOSIT and WITHDRAWAL
        if (transaction.getTransactionType() == Transaction.TransactionType.DEPOSIT) {
            boolean success = deposit(transaction.getAmount());
            if (success) {
                return "✅ DEPOSIT processed into savings. Ref: "
                        + transaction.getReferenceId();
            } else {
                return "❌ DEPOSIT failed. Ref: "
                        + transaction.getReferenceId();
            }
        }

        if (transaction.getTransactionType() == Transaction.TransactionType.WITHDRAWAL) {
            boolean success = withdraw(transaction.getAmount());
            if (success) {
                return "✅ WITHDRAWAL processed from savings. Ref: "
                        + transaction.getReferenceId();
            } else {
                return "❌ WITHDRAWAL failed. Ref: "
                        + transaction.getReferenceId();
            }
        }

        return "⚠️ Savings accounts do not support: "
                + transaction.getTransactionType();
    }

    // toString
    @Override
    public String toString() {
        return "SavingsAccount{" +
                "accountId='"     + getAccountId()  + '\'' +
                ", balance="      + getBalance()    +
                ", interestRate=" + interestRate    +
                ", targetAmount=" + targetAmount    +
                ", withdrawals="  + withdrawalCount +
                "/" + MAX_WITHDRAWALS               +
                '}';
    }

}
