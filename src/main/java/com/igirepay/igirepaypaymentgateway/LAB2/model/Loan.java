package com.igirepay.igirepaypaymentgateway.LAB2.model;

import java.time.LocalDateTime;


public class Loan {
    
    
    public static final String STATUS_PENDING = "PENDING";      
    public static final String STATUS_ACTIVE = "ACTIVE";        
    public static final String STATUS_PAID = "PAID";            
    public static final String STATUS_OVERDUE = "OVERDUE";      
    public static final String STATUS_DEFAULTED = "DEFAULTED";  
    public static final String STATUS_REJECTED = "REJECTED";    

    private int id;
    private int customerId;
    private int accountId;              
    private double principalAmount;      
    private double interestRate;         
    private double totalAmount;          
    private double amountPaid;
    private double remainingBalance;
    private int durationMonths;         
    private LocalDateTime applicationDate;
    private LocalDateTime approvalDate;
    private LocalDateTime disbursementDate;
    private LocalDateTime dueDate;
    private LocalDateTime lastPaymentDate;
    private String status;
    private String purpose;              
    private Integer approvedBy;          
    private String rejectionReason;

   
    public Loan(int customerId, int accountId, double principalAmount, 
                double interestRate, int durationMonths, String purpose) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.durationMonths = durationMonths;
        this.purpose = purpose;
        this.totalAmount = calculateTotalAmount(principalAmount, interestRate);
        this.remainingBalance = this.totalAmount;
        this.amountPaid = 0;
        this.applicationDate = LocalDateTime.now();
        this.status = STATUS_PENDING;
    }

   
    public Loan(int id, int customerId, int accountId, double principalAmount,
                double interestRate, double totalAmount, double amountPaid,
                double remainingBalance, int durationMonths,
                LocalDateTime applicationDate, LocalDateTime approvalDate,
                LocalDateTime disbursementDate, LocalDateTime dueDate,
                LocalDateTime lastPaymentDate, String status, String purpose,
                Integer approvedBy, String rejectionReason) {
        this.id = id;
        this.customerId = customerId;
        this.accountId = accountId;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.remainingBalance = remainingBalance;
        this.durationMonths = durationMonths;
        this.applicationDate = applicationDate;
        this.approvalDate = approvalDate;
        this.disbursementDate = disbursementDate;
        this.dueDate = dueDate;
        this.lastPaymentDate = lastPaymentDate;
        this.status = status;
        this.purpose = purpose;
        this.approvedBy = approvedBy;
        this.rejectionReason = rejectionReason;
    }

    
    private double calculateTotalAmount(double principal, double rate) {
        return principal * (1 + rate / 100);
    }
    public boolean isOverdue() {
        return (STATUS_ACTIVE.equals(status) || STATUS_OVERDUE.equals(status)) &&
               dueDate != null &&
               LocalDateTime.now().isAfter(dueDate) &&
               remainingBalance > 0;
    }
    public boolean needsReminder() {
        if (dueDate == null || !STATUS_ACTIVE.equals(status)) return false;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderDate = dueDate.minusDays(7);
        return now.isAfter(reminderDate) && now.isBefore(dueDate) && remainingBalance > 0;
    }

    
    public long daysUntilDue() {
        if (dueDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
    }

    
    public long daysOverdue() {
        if (dueDate == null || !isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
    }

   
    public int getId() {
         return id; }
    public void setId(int id) {
         this.id = id; }

    public int getCustomerId() {
         return customerId; }
    public void setCustomerId(int customerId) {
         this.customerId = customerId; }

    public int getAccountId() {
         return accountId; }
    public void setAccountId(int accountId) {
         this.accountId = accountId; }

    public double getPrincipalAmount() { 
        return principalAmount; }
    public void setPrincipalAmount(double principalAmount) {
         this.principalAmount =
          principalAmount; }

    public double getInterestRate() { 
        return interestRate; }
    public void setInterestRate(double interestRate) {
         this.interestRate = interestRate; }

    public double getTotalAmount() { 
        return totalAmount; }
    public void setTotalAmount(double totalAmount) {
         this.totalAmount = totalAmount; }

    public double getAmountPaid() {
         return amountPaid; }
    public void setAmountPaid(double amountPaid) {
         this.amountPaid = amountPaid; }

    public double getRemainingBalance() {
         return remainingBalance; }
    public void setRemainingBalance(double remainingBalance) {
         this.remainingBalance = remainingBalance; }

    public int getDurationMonths() {
         return durationMonths; }
    public void setDurationMonths(int durationMonths) {
         this.durationMonths = durationMonths; }

    public LocalDateTime getApplicationDate() {
         return applicationDate; }
    public void setApplicationDate(LocalDateTime applicationDate) { this.applicationDate = applicationDate; }

    public LocalDateTime getApprovalDate() {
         return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }

    public LocalDateTime getDisbursementDate() {
         return disbursementDate; }
    public void setDisbursementDate(LocalDateTime disbursementDate) { this.disbursementDate = disbursementDate; }

    public LocalDateTime getDueDate() { 
        return dueDate; }
    public void setDueDate(LocalDateTime dueDate) {
         this.dueDate = dueDate; }

    public LocalDateTime getLastPaymentDate() {
         return lastPaymentDate; }
    public void setLastPaymentDate(LocalDateTime lastPaymentDate) {
         this.lastPaymentDate = lastPaymentDate; }

    public String getStatus() {
         return status; }
    public void setStatus(String status) { 
        this.status = status; }

    public String getPurpose() {
         return purpose; }
    public void setPurpose(String purpose) {
         this.purpose = purpose; }

    public Integer getApprovedBy() {
         return approvedBy; }
    public void setApprovedBy(Integer approvedBy) {
         this.approvedBy = approvedBy; }

    public String getRejectionReason() { 
        return rejectionReason; }
    public void setRejectionReason(String rejectionReason) {
         this.rejectionReason = rejectionReason; }

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", principal=" + principalAmount +
                ", total=" + totalAmount +
                ", remaining=" + remainingBalance +
                ", status='" + status + '\'' +
                ", dueDate=" + dueDate +
                '}';
    }
}
