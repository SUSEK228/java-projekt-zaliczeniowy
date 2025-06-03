package com.financetracker.financetracker;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:sqlite:transactions.db";

    private static double limitAmount = 0;
    private static String limitPeriod = "Miesięczny";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            return null;
        }
    }

    public static void setLimit(double amount, String period) {
        limitAmount = amount;
        limitPeriod = period;
    }

    public static boolean isLimitExceeded(Transaction newTrans) {
        if (newTrans.getAmount() >= 0) {
            // Jeśli to nie jest wydatek, to limit nie dotyczy
            return false;
        }

        LocalDate startDate = switch (limitPeriod) {
            case "Dzienny" -> LocalDate.now();
            case "Tygodniowy" -> LocalDate.now().minusDays(6);
            case "Miesięczny" -> LocalDate.now().withDayOfMonth(1);
            default -> LocalDate.now();
        };

        double wydatki = 0;
        for (Transaction t : getTransactions()) {
            try {
                LocalDate txDate = LocalDate.parse(t.getDate());
                if (!txDate.isBefore(startDate) && t.getAmount() < 0) {
                    wydatki += Math.abs(t.getAmount()); // liczymy tylko wydatki
                }
            } catch (Exception ignored) {}
        }

        double nowyWydatek = Math.abs(newTrans.getAmount());

        return (wydatki + nowyWydatek) > limitAmount;
    }


    public static List<Transaction> getTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getDouble("amount"),
                        rs.getString("date"),
                        rs.getString("category")
                );
                list.add(t);
            }

        } catch (SQLException e) {
            System.err.println("Błąd pobierania transakcji: " + e.getMessage());
        }

        return list;
    }
}
