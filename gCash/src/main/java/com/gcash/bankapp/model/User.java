package com.gcash.bankapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String username;
    private String password;
    private boolean isAdmin;
    private double balance;
    private List<String> transactionHistory;

    public User(String username, String password, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.balance = 0.0;
        this.transactionHistory = new ArrayList<>();
    }

    // getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public double getBalance() {
        return balance;
    }

    public List<String> getTransactionHistory() {
        return transactionHistory;
    }

    // setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // deposit
    public void deposit(double amount) {
        balance += amount;
        transactionHistory.add("Deposited ₱" + amount);
    }

    //  withdraw 
    public boolean withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            transactionHistory.add("Withdrew ₱" + amount);
            return true;
        }
        return false;
    }

    // transaction logging
    public void addTransaction(String log) {
        transactionHistory.add(log);
    }
}
