package com.igirepay.igirepaypaymentgateway.LAB2.model;

import java.time.LocalDateTime;


public class Customer {
    public static final String ROLE_USER  = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final int MAX_FAILED_ATTEMPTS = 3;

    private int           id;
    private String        fullName;
    private String        email;
    private String        phoneNumber;
    private String        pin;
    private String        role;               
    private int           failedPinAttempts;  
    private boolean       isLocked;           
    private LocalDateTime createdAt;
    public Customer(String fullName, String email, String phoneNumber, String pin) {
        this.fullName          = fullName;
        this.email             = email;
        this.phoneNumber       = phoneNumber;
        this.pin               = pin;
        this.role              = ROLE_USER;   
        
        this.failedPinAttempts = 0;
        this.isLocked          = false;
    }
    public Customer(int id, String fullName, String email, String phoneNumber,
                    String pin, String role, int failedPinAttempts,
                    boolean isLocked, LocalDateTime createdAt) {
        this.id                = id;
        this.fullName          = fullName;
        this.email             = email;
        this.phoneNumber       = phoneNumber;
        this.pin               = pin;
        this.role              = role;
        this.failedPinAttempts = failedPinAttempts;
        this.isLocked          = isLocked;
        this.createdAt         = createdAt;
    }
    public boolean isAdmin() {
         return ROLE_ADMIN.equals(role); 
    }
    public boolean isUser()  { 
        return ROLE_USER.equals(role); 
    }
    public int    getId(){ 
        return id; 
    }
    public void   setId(int id){
         this.id = id; 
    }

    public String getFullName() {
         return fullName; 
    }
    public void   setFullName(String fullName)   {
         this.fullName = fullName;
     }

    public String getEmail(){
         return email; 
    }
    public void   setEmail(String email)   { 
        this.email = email; 
    }

    public String getPhoneNumber(){
         return phoneNumber;
     }
    public void   setPhoneNumber(String phoneNumber)   { 
        this.phoneNumber = phoneNumber; 
    }

    public String getPin(){
         return pin; 
    }
    public void   setPin(String pin)   { 
        this.pin = pin;
     }

    public String getRole() { 
        return role; 
    }
    public void   setRole(String role)     {
         this.role = role; 
    }

    public int  getFailedPinAttempts() {
         return failedPinAttempts;
     }
    public void setFailedPinAttempts(int failedPinAttempts)  { 
        this.failedPinAttempts = failedPinAttempts; 
    }

    public boolean isLocked()                { 
        return isLocked; 
    }
    public void    setLocked(boolean locked) { 
        this.isLocked = locked; 
    }

    public LocalDateTime getCreatedAt(){ 
        return createdAt; 
    }
    public void          setCreatedAt(LocalDateTime dt) {
         this.createdAt = dt; 
    }

    @Override
    public String toString() {
        return "Customer{id=" + id +
                ", name='"    + fullName          + '\'' +
                ", email='"   + email             + '\'' +
                ", phone='"   + phoneNumber       + '\'' +
                ", role='"    + role              + '\'' +
                ", locked="   + isLocked          +
                '}';
    }
}
