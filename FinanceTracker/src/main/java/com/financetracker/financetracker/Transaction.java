package com.financetracker.financetracker;

public class Transaction {
    private String description;
    private double amount;
    private String category;

    public Transaction(String description, double amount, String category) {
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return description + " (" + amount + " zł) [" + category + "]";
    }

}
