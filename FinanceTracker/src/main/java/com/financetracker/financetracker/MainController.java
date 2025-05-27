package com.financetracker.financetracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import java.time.LocalDate;
import java.time.YearMonth;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.DatePicker;

public class MainController {
    // FXML bindings
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> titleColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Void> actionColumn;

    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;

    @FXML private Label allAccountsLabel;
    @FXML private Label incomeThisMonthLabel;
    @FXML private Label expensesThisMonthLabel;

    // filter bar
    @FXML private HBox filterBar;
    @FXML private TextField descriptionFilter;
    @FXML private DatePicker dateFilter;
    @FXML private Button filterButton;
    @FXML private Button clearButton;

    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

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
        updateSummaryLabels();
        addDeleteButtonToTable();

        /* RESPONSYWNOŚĆ TABELI */
        titleColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.30));
        amountColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.20));
        dateColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.20));
        categoryColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.20));
        actionColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.10));

        titleColumn.setStyle("-fx-alignment: CENTER;");
        amountColumn.setStyle("-fx-alignment: CENTER;"); // lub CENTER-RIGHT dla liczb
        dateColumn.setStyle("-fx-alignment: CENTER;");
        categoryColumn.setStyle("-fx-alignment: CENTER;");
        actionColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void createTableIfNotExists() {
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
        transactions.setAll(getTransactions());
        transactionTable.setItems(transactions);
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
        String amountText  = amountField.getText();
        String category    = categoryComboBox.getValue();

        if (description.isEmpty() || amountText.isEmpty() || category == null) {
            System.out.println("Uzupełnij wszystkie pola!");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            String date   = LocalDate.now().toString();

            Transaction t = new Transaction(description, amount, date, category);
            addTransaction(t);
            loadTransactions();
            updateSummaryLabels();

            // wyczyść formularz
            descriptionField.clear();
            amountField.clear();
            categoryComboBox.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            System.out.println("Nieprawidłowa kwota.");
        }
    }

    private void updateSummaryLabels() {
        double totalIncome   = 0;
        double totalExpense  = 0;
        double monthlyIncome = 0;
        double monthlyExpense= 0;

        YearMonth currentMonth = YearMonth.now();
        for (Transaction t : transactionTable.getItems()) {
            double   amount = t.getAmount();
            LocalDate date   = LocalDate.parse(t.getDate());

            if (amount > 0) {
                totalIncome += amount;
                if (YearMonth.from(date).equals(currentMonth)) {
                    monthlyIncome += amount;
                }
            } else if (amount < 0) {
                totalExpense += Math.abs(amount);
                if (YearMonth.from(date).equals(currentMonth)) {
                    monthlyExpense += Math.abs(amount);
                }
            }
        }

        double balance = totalIncome - totalExpense;

        allAccountsLabel.setText(String.format("%.2f PLN", balance));
        incomeThisMonthLabel.setText(String.format("%.2f PLN", monthlyIncome));
        expensesThisMonthLabel.setText(String.format("%.2f PLN", monthlyExpense));
    }

    private void addDeleteButtonToTable() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("🗑");

            {
                deleteButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    deleteTransaction(transaction);
                    loadTransactions();
                    updateSummaryLabels();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
    }

    private void deleteTransaction(Transaction transaction) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transaction.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Błąd podczas usuwania transakcji: " + e.getMessage());
        }
    }

    // Filtrowanie
    @FXML
    private void onFilter(ActionEvent event) {
        String desc = descriptionFilter.getText().toLowerCase().trim();
        LocalDate date = dateFilter.getValue();

        ObservableList<Transaction> filtered = transactions.filtered(tx -> {
            boolean matchesDesc = desc.isEmpty() || tx.getTitle().toLowerCase().contains(desc);
            boolean matchesDate = date == null || tx.getDate().equals(date.toString());
            return matchesDesc && matchesDate;
        });

        transactionTable.setItems(filtered);
    }

    @FXML
    private void onClear(ActionEvent event) {
        descriptionFilter.clear();
        dateFilter.setValue(null);
        transactionTable.setItems(transactions); // pokazuje ponownie wszystkie transakcje
    }
}
