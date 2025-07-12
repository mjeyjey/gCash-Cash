package com.gcash.bankapp.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gcash.bankapp.model.User;
import com.gcash.bankapp.service.UserService;

@Controller
public class TransactionController {

    @Autowired
    private UserService userService;

    // returns path to the user's balance file
    private String getBalanceFilePath(String username) {
        return "data/" + username + "_balance.dat";
    }

    // returns path to the user's transaction history file
    private String getHistoryFilePath(String username) {
        return "data/" + username + "_history.dat";
    }

    // loads balance for a user from file
    private double loadBalance(String username) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(getBalanceFilePath(username)))) {
            return in.readDouble();
        } catch (IOException e) {
            return 0.0; // default to zero if file doesn't exist
        }
    }

    // saves balance for a user to file
    private void saveBalance(String username, double balance) {
        try {
            new File("data").mkdirs(); // ensure the "data" folder exists
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(getBalanceFilePath(username)))) {
                out.writeDouble(balance);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // loads transaction history list from file
    private List<String> loadHistory(String username) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(getHistoryFilePath(username)))) {
            return (List<String>) in.readObject();
        } catch (Exception e) {
            return new ArrayList<>(); // return empty list if no history
        }
    }

    // saves transaction history list to file
    private void saveHistory(String username, List<String> history) {
        try {
            new File("data").mkdirs(); // ensure the "data" folder exists
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getHistoryFilePath(username)))) {
                out.writeObject(history);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // shows deposit form
    @GetMapping("/deposit")
    public String showDeposit() {
        return "deposit";
    }

    // process deposit transaction
    @PostMapping("/deposit")
    public String doDeposit(@RequestParam double amount, Model model) {
        User user = userService.getCurrentUser(); //  get the current logged-in user
        String username = user.getUsername();

        double balance = loadBalance(username);
        balance += amount; // update balance
        saveBalance(username, balance); //  save to file

        List<String> history = loadHistory(username);
        String log = "Deposited ₱" + amount + " on " + LocalDateTime.now();
        history.add(log); // add to history
        saveHistory(username, history);

        model.addAttribute("message", log);
        model.addAttribute("balance", balance);
        model.addAttribute("user", user); // required for receipt.html
        return "receipt";
    }

    // show withdraw form
    @GetMapping("/withdraw")
    public String showWithdraw() {
        return "withdraw";
    }

    // process withdrawal
    @PostMapping("/withdraw")
    public String doWithdraw(@RequestParam double amount, Model model) {
        User user = userService.getCurrentUser();
        String username = user.getUsername();

        double balance = loadBalance(username);
        if (amount > balance) {
            model.addAttribute("message", "❌ Insufficient balance.");
        } else {
            balance -= amount;
            saveBalance(username, balance);

            List<String> history = loadHistory(username);
            String log = "Withdrew ₱" + amount + " on " + LocalDateTime.now();
            history.add(log);
            saveHistory(username, history);

            model.addAttribute("message", log);
        }

        model.addAttribute("balance", balance);
        model.addAttribute("user", user);
        return "receipt";
    }

    // cash-in form
    @GetMapping("/cashin")
    public String showCashIn() {
        return "cashin";
    }

    // processes cash-in
    @PostMapping("/cashin")
    public String doCashIn(@RequestParam double amount, Model model) {
        User user = userService.getCurrentUser();
        String username = user.getUsername();

        double balance = loadBalance(username);
        balance += amount;
        saveBalance(username, balance);

        List<String> history = loadHistory(username);
        String log = "Cashed In ₱" + amount + " on " + LocalDateTime.now();
        history.add(log);
        saveHistory(username, history);

        model.addAttribute("message", log);
        model.addAttribute("balance", balance);
        model.addAttribute("user", user);
        return "receipt";
    }

    // shows send money form
    @GetMapping("/send")
    public String showSend() {
        return "send";
    }

    // process send transaction
    @PostMapping("/send")
    public String doSend(@RequestParam double amount,
                         @RequestParam String recipient,
                         Model model) {
        User user = userService.getCurrentUser();
        String username = user.getUsername();

        double balance = loadBalance(username);
        if (amount > balance) {
            model.addAttribute("message", "❌ Insufficient balance.");
        } else {
            balance -= amount;
            saveBalance(username, balance);

            List<String> history = loadHistory(username);
            String log = "Sent ₱" + amount + " to " + recipient + " on " + LocalDateTime.now();
            history.add(log);
            saveHistory(username, history);

            model.addAttribute("message", log);
        }

        model.addAttribute("balance", balance);
        model.addAttribute("user", user);
        return "receipt";
    }

    // display transaction history
    @GetMapping("/transaction")
    public String showHistory(Model model) {
        User user = userService.getCurrentUser();
        String username = user.getUsername();

        List<String> history = loadHistory(username);
        model.addAttribute("transaction", history);
        return "transaction";
    }
}
