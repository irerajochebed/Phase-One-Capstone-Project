package com.igirepay.igirepaypaymentgateway.LAB1.model;


import java.util.List;


public class Customer {


    private String        customerId;
    private String        fullName;
    private String        email;
    private String        phoneNumber;
    private String        pin;
    private List<Account> accounts;


public Customer(String customerId, String fullName, String email, String phoneNumber, String pin, List<Account> accounts) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.accounts = accounts;

    }

    public Customer(String cust002, String bobNkurunziza, String mail, String number, String number1) {
    }

    public void addAccount(Account account) {
        accounts.add(account);
        System.out.println("[Customer] Account " + account.getAccountId()
                + " added to customer " + fullName);
    }


    public void removeAccount(String accountId) {
        accounts.removeIf(acc -> acc.getAccountId().equals(accountId));
    }


    public Account getAccountById(String accountId) {
        for (Account acc : accounts) {
            if (acc.getAccountId().equals(accountId)) {
                return acc;
            }
        }
        return null;
    }


    public boolean validatePin(String inputPin) {
        return this.pin.equals(inputPin);
    }


    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void setPin(String pin) { this.pin = pin; }

    public List<Account> getAccounts() { return accounts; }


    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId  + '\'' +
                ", fullName='" + fullName    + '\'' +
                ", email='"   + email        + '\'' +
                ", phone='"   + phoneNumber  + '\'' +
                ", accounts=" + accounts.size() + " account(s)" +
                '}';
    }
}
