package com.financetracker.financetracker;

public class Transaction {
    private int id;
    private String title;
    private double amount;
    private String date;
    private String category;

    // Konstruktor dla nowych transakcji
    public Transaction(String title, double amount, String date, String category) {
        this(-1, title, amount, date, category);
    }

    // Konstruktor dla istniejących transakcji
    public Transaction(int id, String title, double amount, String date, String category) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    // Gettery i settery
    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getCategory() { return category; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(String date) { this.date = date; }
    public void setCategory(String category) { this.category = category; }
}