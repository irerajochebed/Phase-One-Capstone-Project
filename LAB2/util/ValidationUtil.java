package com.igirepay.igirepaypaymentgateway.LAB2.util;

import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;


public class ValidationUtil {

   
    public static void validatePhoneNumber(String phoneNumber) throws IgirePayException {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_PHONE_NUMBER,
                "Phone number cannot be empty"
            );
        }
        String cleaned = phoneNumber.replaceAll("[\\s-]", "");
        if (!cleaned.matches("\\d{10}")) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_PHONE_NUMBER,
                "Phone number must be exactly 10 digits. Got: " + phoneNumber
            );
        }
        if (!cleaned.startsWith("078") && !cleaned.startsWith("079")) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_PHONE_NUMBER,
                "Rwandan phone number must start with 078 or 079. Got: " + phoneNumber
            );
        }
    }

   
    public static void validateEmail(String email) throws IgirePayException {
        if (email == null || email.trim().isEmpty()) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_EMAIL,
                "Email cannot be empty"
            );
        }

        String trimmed = email.trim();
        if (!trimmed.contains("@")) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_EMAIL,
                "Email must contain @. Got: " + email
            );
        }
        if (!trimmed.contains(".")) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_EMAIL,
                "Email must contain a dot (.). Got: " + email
            );
        }
        int atIndex = trimmed.indexOf("@");
        int dotIndex = trimmed.lastIndexOf(".");
        if (atIndex > dotIndex) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_EMAIL,
                "Email format is invalid. @ must come before . Got: " + email
            );
        }
    }

  
    public static double calculateTransactionFee(double amount) {
        if (amount >= 1 && amount <= 1000) {
            return 20.0;
        } else if (amount >= 1001 && amount <= 5000) {
            return 100.0;
        } else if (amount >= 5001 && amount <= 30000) {
            return 250.0;
        } else if (amount >= 30001 && amount <= 50000) {
            return 600.0;
        } else if (amount >= 50001 && amount <= 100000) {
            return 800.0;
        } else if (amount >= 100001 && amount <= 1000000) {
            return 1000.0;
        } else {
              return 0.0;
        }
    }
}
