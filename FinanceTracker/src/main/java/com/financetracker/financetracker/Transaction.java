package com.financetracker.financetracker;

public class Transaction {
    private int id;
    private String title;
    private double amount;
    private String date;

    public Transaction(int id, String title, double amount, String date) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.date = date;
    }

    public Transaction(String title, double amount, String date) {
        this(-1, title, amount, date);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(String date) { this.date = date; }
}