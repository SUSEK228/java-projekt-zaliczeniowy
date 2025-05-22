package com.financetracker.financetracker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;



public class MainController {
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> titleColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;



    @FXML
    public void initialize() {
        createTableIfNotExists();

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Jedzenie", "Transport", "Rozrywka", "Zdrowie", "Inne"
        ));

        loadTransactions();
    }

    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                category TEXT NOT NULL
            );
        """;

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Failed to create table: " + e.getMessage());
        }
    }

    public void loadTransactions() {
        List<Transaction> transactions = getTransactions();
        ObservableList<Transaction> data = FXCollections.observableArrayList(transactions);
        transactionTable.setItems(data);
    }

    public void addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions(title, amount, date, category) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getTitle());
            pstmt.setDouble(2, t.getAmount());
            pstmt.setString(3, t.getDate());
            pstmt.setString(4, t.getCategory());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Insert failed: " + e.getMessage());
        }
    }

    public List<Transaction> getTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions";

        try (Connection conn = Database.connect();
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
            System.err.println("Błąd podczas pobierania danych z bazy: " + e.getMessage());
        }

        return list;
    }
    @FXML
    private void handleAddTransaction(ActionEvent event) {
        String description = descriptionField.getText();
        String amountText = amountField.getText();
        String category = categoryComboBox.getValue();

        if (description.isEmpty() || amountText.isEmpty() || category == null) {
            System.out.println("Uzupełnij wszystkie pola!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            String date = java.time.LocalDate.now().toString();

            Transaction t = new Transaction(description, amount, date, category); // Można dodać kategorię jeśli rozbudujesz klasę
            addTransaction(t);
            loadTransactions();

            // wyczyść formularz
            descriptionField.clear();
            amountField.clear();
            categoryComboBox.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            System.out.println("Nieprawidłowa kwota.");
        }
    }

}
