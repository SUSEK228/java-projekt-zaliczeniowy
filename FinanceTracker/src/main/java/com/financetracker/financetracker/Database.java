package com.financetracker.financetracker;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:sqlite:transactions.db";

    // Limit wydatków
    private static double limitAmount = 0;
    private static String limitPeriod = "Miesięczny";

    // Lista testowa (do unit testów)
    private static List<Transaction> testTransactions = null;

    // Połączenie z bazą danych
    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            return null;
        }
    }

    // Ustawienie limitu
    public static void setLimit(double amount, String period) {
        limitAmount = amount;
        limitPeriod = period;
    }

    // Sprawdzenie czy przekroczono limit wydatków
    public static boolean isLimitExceeded(Transaction newTrans) {
        if (newTrans.getAmount() >= 0) {
            return false; // tylko wydatki (ujemne kwoty)
        }

        LocalDate startDate = switch (limitPeriod) {
            case "Dzienny" -> LocalDate.now();
            case "Tygodniowy" -> LocalDate.now().minusDays(6);
            case "Miesięczny" -> LocalDate.now().withDayOfMonth(1);
            default -> LocalDate.now();
        };

        double spent = 0;
        for (Transaction t : getTransactions()) {
            try {
                LocalDate txDate = LocalDate.parse(t.getDate());
                if (!txDate.isBefore(startDate) && t.getAmount() < 0) {
                    spent += Math.abs(t.getAmount());
                }
            } catch (Exception ignored) {}
        }

        double current = Math.abs(newTrans.getAmount());
        return (spent + current) > limitAmount;
    }

    // Pobieranie transakcji z bazy (lub danych testowych)
    public static List<Transaction> getTransactions() {
        if (testTransactions != null) {
            return testTransactions;
        }

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

    // 👇 Wstrzyknięcie danych testowych (dla testów jednostkowych)
    public static void injectTransactionList(List<Transaction> list) {
        testTransactions = list;
    }

    // 👇 Wyczyszczenie danych testowych (np. w @AfterEach)
    public static void clearInjectedTransactions() {
        testTransactions = null;
    }
}
