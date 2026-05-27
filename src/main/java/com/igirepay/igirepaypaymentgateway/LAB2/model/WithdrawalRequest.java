package com.igirepay.igirepaypaymentgateway.LAB2.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public class WithdrawalRequest {

    private int id;
    private int accountId;
    private int customerId;
    private double amount;
    private LocalDateTime requestDate;
    private LocalDateTime availableDate;
    private String status;  
    private LocalDateTime processedDate;
    private String referenceId;
    private LocalDateTime createdAt;

   
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_PROCESSED = "PROCESSED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    
    public WithdrawalRequest(int accountId, int customerId, double amount, 
                            LocalDateTime availableDate, String referenceId) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.amount = amount;
        this.requestDate = LocalDateTime.now();
        this.availableDate = availableDate;
        this.status = STATUS_PENDING;
        this.referenceId = referenceId;
    }

    
    public WithdrawalRequest(int id, int accountId, int customerId, double amount,
                            LocalDateTime requestDate, LocalDateTime availableDate,
                            String status, LocalDateTime processedDate, 
                            String referenceId, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.customerId = customerId;
        this.amount = amount;
        this.requestDate = requestDate;
        this.availableDate = availableDate;
        this.status = status;
        this.processedDate = processedDate;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
    }

    
    public boolean isAvailable() {
        return LocalDateTime.now().isAfter(availableDate) || 
               LocalDateTime.now().isEqual(availableDate);
    }

    public long getHoursRemaining() {
        if (isAvailable()) return 0;
        return ChronoUnit.HOURS.between(LocalDateTime.now(), availableDate);
    }

    public long getMinutesRemaining() {
        if (isAvailable()) return 0;
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), availableDate);
    }

    

    public int getId() {
         return id; }
    public void setId(int id) {
         this.id = id; }

    public int getAccountId() {
         return accountId; }
    public void setAccountId(int accountId) {
         this.accountId = accountId; }

    public int getCustomerId() {
         return customerId; }
    public void setCustomerId(int customerId) {
         this.customerId = customerId; }

    public double getAmount() { 
        return amount; }
    public void setAmount(double amount) {
         this.amount = amount; }

    public LocalDateTime getRequestDate() { 
        return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { 
        this.requestDate = requestDate; }

    public LocalDateTime getAvailableDate() {
         return availableDate; }
    public void setAvailableDate(LocalDateTime availableDate) {
         this.availableDate = availableDate; }

    public String getStatus() {
         return status; }
    public void setStatus(String status) {
         this.status = status; }

    public LocalDateTime getProcessedDate() {
         return processedDate; }
    public void setProcessedDate(LocalDateTime processedDate) {
         this.processedDate = processedDate; }

    public String getReferenceId() {
         return referenceId; }
    public void setReferenceId(String referenceId) {
         this.referenceId = referenceId; }

    public LocalDateTime getCreatedAt() {
         return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
         this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "WithdrawalRequest{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", availableDate=" + availableDate +
                ", hoursRemaining=" + getHoursRemaining() +
                '}';
    }
}
