package com.gcash.bankapp.controller;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gcash.bankapp.model.User;
import com.gcash.bankapp.service.UserService;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;

    public static class UserData {
        private String username;
        private double balance;
        private int transactionCount;

        public UserData(String username, double balance, int transactionCount) {
            this.username = username;
            this.balance = balance;
            this.transactionCount = transactionCount;
        }

        public String getUsername() {
            return username;
        }

        public double getBalance() {
            return balance;
        }

        public int getTransactionCount() {
            return transactionCount;
        }
    }

    private String getBalanceFile(String username) {
        return "data/" + username + "_balance.dat";
    }

    private String getHistoryFile(String username) {
        return "data/" + username + "_history.dat";
    }

    private double getBalanceFor(String username) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(getBalanceFile(username)))) {
            return in.readDouble();
        } catch (IOException e) {
            return 0.0;
        }
    }

    private List<String> getHistoryFor(String username) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(getHistoryFile(username)))) {
            return (List<String>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @GetMapping("/admin_dashboard")
    public String adminDashboard(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/";
        }

        Map<String, User> allUsers = userService.getAllUsers();
        List<UserData> userDataList = new ArrayList<>();

        for (String username : allUsers.keySet()) {
            if ("admin".equalsIgnoreCase(username)) continue; // exclude admin
            double balance = getBalanceFor(username);
            int historySize = getHistoryFor(username).size();
            userDataList.add(new UserData(username, balance, historySize));
        }

        model.addAttribute("user", currentUser);           // admin display
        model.addAttribute("allUsers", userDataList);      // user table
        return "admin_dashboard";
    }
}
