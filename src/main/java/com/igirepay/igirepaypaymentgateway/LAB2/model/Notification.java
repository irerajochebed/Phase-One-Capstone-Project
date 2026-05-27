package com.igirepay.igirepaypaymentgateway.LAB2.model;

import java.time.LocalDateTime;


public class Notification {

    private int           id;
    private int           customerId;
    private String        type;          
    private String        title;
    private String        message;
    private double        amount;
    private double        fee;
    private String        referenceId;
    private boolean       isRead;
    private String        senderName;
    private String        receiverName;
    private boolean       actionRequired;
    private String        actionType;    
    private Integer       relatedTransactionId;
    private LocalDateTime createdAt;

    // Notification types
    public static final String TYPE_SENT            = "SENT";
    public static final String TYPE_RECEIVED        = "RECEIVED";
    public static final String TYPE_DEPOSIT         = "DEPOSIT";
    public static final String TYPE_WITHDRAWAL      = "WITHDRAWAL";
    public static final String TYPE_ADMIN_MESSAGE   = "ADMIN_MESSAGE";
    public static final String TYPE_PASSWORD_RESET  = "PASSWORD_RESET";
    public static final String TYPE_DISPUTE_UPDATE  = "DISPUTE_UPDATE";
    public static final String TYPE_ACCOUNT_UNLOCKED = "ACCOUNT_UNLOCKED";
    public static final String TYPE_FROZEN_FUNDS    = "FROZEN_FUNDS";


    public Notification(int customerId, String type, String title, String message,
                       double amount, double fee, String referenceId) {
        this.customerId  = customerId;
        this.type        = type;
        this.title       = title;
        this.message     = message;
        this.amount      = amount;
        this.fee         = fee;
        this.referenceId = referenceId;
        this.isRead      = false;
        this.actionRequired = false;
    }

   
    public Notification(int customerId, String type, String title, String message,
                       double amount, double fee, String referenceId,
                       String senderName, String receiverName, 
                       boolean actionRequired, String actionType, Integer relatedTransactionId) {
        this.customerId  = customerId;
        this.type        = type;
        this.title       = title;
        this.message     = message;
        this.amount      = amount;
        this.fee         = fee;
        this.referenceId = referenceId;
        this.senderName  = senderName;
        this.receiverName = receiverName;
        this.actionRequired = actionRequired;
        this.actionType  = actionType;
        this.relatedTransactionId = relatedTransactionId;
        this.isRead      = false;
    }

    
    public Notification(int id, int customerId, String type, String title, String message,
                       double amount, double fee, String referenceId, boolean isRead,
                       String senderName, String receiverName, boolean actionRequired,
                       String actionType, Integer relatedTransactionId, LocalDateTime createdAt) {
        this.id          = id;
        this.customerId  = customerId;
        this.type        = type;
        this.title       = title;
        this.message     = message;
        this.amount      = amount;
        this.fee         = fee;
        this.referenceId = referenceId;
        this.isRead      = isRead;
        this.senderName  = senderName;
        this.receiverName = receiverName;
        this.actionRequired = actionRequired;
        this.actionType  = actionType;
        this.relatedTransactionId = relatedTransactionId;
        this.createdAt   = createdAt;
    }

    
    public int    getId()                     {
        
         return id; }
    public void   setId(int id)               {
        
         this.id = id; }

    public int    getCustomerId()             {
        
         return customerId; }
    public void   setCustomerId(int id)       {
        
         this.customerId = id; }

    public String getType()                   {
        
         return type; }
    public void   setType(String type)        {
        
         this.type = type; }

    public String getTitle()                  {
        
         return title; }
    public void   setTitle(String title)      {
        
         this.title = title; }

    public String getMessage()                {
        
         return message; }
    public void   setMessage(String msg)      {
        
         this.message = msg; }

    public double getAmount()                 { 
        
        return amount; }
    public void   setAmount(double amount)    {
        
         this.amount = amount; }

    public double getFee()                    {
        
         return fee; }
    public void   setFee(double fee)          {
        
         this.fee = fee; }

    public String getReferenceId()            {
         return referenceId; }
    public void   setReferenceId(String ref)  {
         this.referenceId = ref; }

    public boolean isRead()                   {
         return isRead; }
    public void    setRead(boolean read)      {
         this.isRead = read; }

    public String getSenderName()             {
         return senderName; }
    public void   setSenderName(String name)  {
         this.senderName = name; }

    public String getReceiverName()           {
         return receiverName; }
    public void   setReceiverName(String name){
         this.receiverName = name; }

    public boolean isActionRequired()         {
         return actionRequired; }
    public void    setActionRequired(boolean req) {
         this.actionRequired = req; }

    public String getActionType()             {
         return actionType; }
    public void   setActionType(String type)  {
         this.actionType = type; }

    public Integer getRelatedTransactionId()  {
         return relatedTransactionId; }
    public void    setRelatedTransactionId(Integer id) {
         this.relatedTransactionId = id; }

    public LocalDateTime getCreatedAt()       {
         return createdAt; }
    public void setCreatedAt(LocalDateTime dt){
         this.createdAt = dt; }

    @Override
    public String toString() {
        return "Notification{id=" + id +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", read=" + isRead +
                ", actionRequired=" + actionRequired +
                '}';
    }
}
