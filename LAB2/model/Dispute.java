package com.igirepay.igirepaypaymentgateway.LAB2.model;

import java.time.LocalDateTime;



public class Dispute {
    
    
    public static final String TYPE_WRONG_RECIPIENT = "WRONG_RECIPIENT";
    public static final String TYPE_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String TYPE_AMOUNT_ERROR = "AMOUNT_ERROR";
    public static final String TYPE_OTHER = "OTHER";
    
    
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_INVESTIGATING = "INVESTIGATING";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_REJECTED = "REJECTED";
    
    private int id;
    private int customerId;
    private int transactionId;
    private String disputeType;
    private String description;
    private String status;
    private String adminNotes;
    private Integer resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    
    
    public Dispute(int customerId, int transactionId, String disputeType, String description) {
        this.customerId = customerId;
        this.transactionId = transactionId;
        this.disputeType = disputeType;
        this.description = description;
        this.status = STATUS_PENDING;
    }
    
    
    public Dispute(int id, int customerId, int transactionId, String disputeType, 
                   String description, String status, String adminNotes, 
                   Integer resolvedBy, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.customerId = customerId;
        this.transactionId = transactionId;
        this.disputeType = disputeType;
        this.description = description;
        this.status = status;
        this.adminNotes = adminNotes;
        this.resolvedBy = resolvedBy;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }
    
    
    public int getId() {
         return id; }
    public void setId(int id) {
         this.id = id; }
    
    public int getCustomerId() {
         return customerId; }
    public void setCustomerId(int customerId) {
         this.customerId = customerId; }
    
    public int getTransactionId() {
         return transactionId; }
    public void setTransactionId(int transactionId) { 
        this.transactionId = transactionId; }
    
    public String getDisputeType() { 
        return disputeType; }
    public void setDisputeType(String disputeType) { 
        this.disputeType = disputeType; }
    
    public String getDescription() { 
        return description; }
    public void setDescription(String description) {
         this.description = description; }
    
    public String getStatus() {
         return status; }
    public void setStatus(String status) { 
        this.status = status; }
    
    public String getAdminNotes() { 
        return adminNotes; }
    public void setAdminNotes(String adminNotes) {
         this.adminNotes = adminNotes; }
    
    public Integer getResolvedBy() { 
        return resolvedBy; }
    public void setResolvedBy(Integer resolvedBy) {
         this.resolvedBy = resolvedBy; }
    
    public LocalDateTime getCreatedAt() {
         return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; }
    
    public LocalDateTime getResolvedAt() { 
        return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) {
         this.resolvedAt = resolvedAt; }
}
