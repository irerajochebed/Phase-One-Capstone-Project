package com.igirepay.igirepaypaymentgateway.LAB3.exception;


public class IgirePayException extends Exception {

    
    public enum ErrorType {
        INVALID_AMOUNT,           
        DUPLICATE_TRANSACTION,    
        INSUFFICIENT_BALANCE,     
        INVALID_ACCOUNT,          
        INVALID_CUSTOMER,         
        INVALID_PIN,              
        DATABASE_ERROR,           
        INVALID_INPUT,            
        INVALID_PHONE_NUMBER,     
        INVALID_EMAIL             
    }
    private final ErrorType errorType; 
    public IgirePayException(ErrorType errorType, String message) {
        super(message);           
        this.errorType = errorType;
    }
    public ErrorType getErrorType() {
        return errorType;
    }
    @Override
    public String toString() {
        return "[" + errorType + "] " + getMessage();
    }
}
