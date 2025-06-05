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
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.YearMonth;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Map;
import java.util.HashMap;
import javafx.scene.chart.PieChart;

/**
 * Kontroler głównego widoku aplikacji Finance Tracker.
 * Obsługuje logikę związaną z: dodawaniem transakcji, tabelą, filtrowaniem, widokiem limitów i statystyk.
 */
public class MainController {

    // === FXML powiązania ===

    // Tabela transakcji i kolumny
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> titleColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Void> actionColumn;

    // Formularz dodawania transakcji
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;

    // Etykiety podsumowania
    @FXML private Label allAccountsLabel;
    @FXML private Label incomeThisMonthLabel;
    @FXML private Label expensesThisMonthLabel;

    // Pasek filtrów
    @FXML private HBox filterBar;
    @FXML private TextField descriptionFilter;
    @FXML private DatePicker dateFilter;
    @FXML private Button filterButton;
    @FXML private Button clearButton;

    // Widok limitów
    @FXML private VBox limitSettingsBox;
    @FXML private TextField limitAmountField;
    @FXML private ComboBox<String> limitPeriodComboBox;

    // Widoki główne
    @FXML private VBox mainContentBox;
    @FXML private VBox statsBox;

    // Statystyki
    @FXML private Label statsBalance;
    @FXML private Label statsIncome;
    @FXML private Label statsExpenses;
    @FXML private Label statsCount;
    @FXML private VBox categoryStatsBox;
    @FXML private PieChart expensePieChart;

    // Dane transakcji
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    /**
     * Inicjalizacja kontrolera — wywoływana automatycznie po załadowaniu FXML.
     * Konfiguruje tabele, ładuje dane i ustawia responsywność kolumn.
     */
    @FXML
    public void initialize() {
        createTableIfNotExists();

        // Powiązania kolumn z polami obiektu Transaction
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Kategorie w ComboBox
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Jedzenie", "Transport", "Rozrywka", "Zdrowie", "Inne"
        ));

        loadTransactions();
        updateSummaryLabels();
        addDeleteButtonToTable();

        // Ustawienie dynamicznej szerokości kolumn
        titleColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.30));
        amountColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.20));
        dateColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.20));
        categoryColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.20));
        actionColumn.prefWidthProperty().bind(transactionTable.widthProperty().multiply(0.10));

        // Wyrównanie zawartości w kolumnach
        titleColumn.setStyle("-fx-alignment: CENTER;");
        amountColumn.setStyle("-fx-alignment: CENTER;");
        dateColumn.setStyle("-fx-alignment: CENTER;");
        categoryColumn.setStyle("-fx-alignment: CENTER;");
        actionColumn.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Tworzy tabelę SQL `transactions`, jeśli jeszcze nie istnieje.
     */
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

    /**
     * Wczytuje transakcje z bazy danych i przypisuje do tabeli.
     */
    public void loadTransactions() {
        transactions.setAll(getTransactions());
        transactionTable.setItems(transactions);
    }

    /**
     * Dodaje nową transakcję do bazy danych.
     */
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

    /**
     * Pobiera wszystkie transakcje z bazy danych.
     */
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

    /**
     * Obsługuje przycisk dodawania transakcji.
     * Waliduje dane, sprawdza limity i aktualizuje UI.
     */
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

            // Sprawdzenie przekroczenia limitu
            if (Database.isLimitExceeded(t)) {
                showAlert("Limit przekroczony", "Nie możesz dodać więcej transakcji w tym okresie.");
                return;
            }

            addTransaction(t);
            loadTransactions();
            updateSummaryLabels();

            // Czyszczenie pól formularza
            descriptionField.clear();
            amountField.clear();
            categoryComboBox.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            System.out.println("Nieprawidłowa kwota.");
        }
    }

    /**
     * Aktualizuje etykiety z podsumowaniem (saldo, przychody, wydatki).
     */
    private void updateSummaryLabels() {
        double totalIncome = 0;
        double totalExpense = 0;
        double monthlyIncome = 0;
        double monthlyExpense = 0;
        YearMonth currentMonth = YearMonth.now();

        for (Transaction t : transactionTable.getItems()) {
            double amount = t.getAmount();
            LocalDate date = LocalDate.parse(t.getDate());

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

    /**
     * Dodaje przyciski usuwania do każdej transakcji w tabeli.
     */
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

    /**
     * Usuwa transakcję z bazy danych.
     */
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

    /**
     * Filtruje transakcje wg opisu i daty.
     */
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

    /**
     * Czyści filtry i przywraca pełną listę transakcji.
     */
    @FXML
    private void onClear(ActionEvent event) {
        descriptionFilter.clear();
        dateFilter.setValue(null);
        transactionTable.setItems(transactions);
    }

    /**
     * Zapisuje nowy limit wydatków do bazy.
     */
    @FXML
    private void handleSaveLimit() {
        try {
            double amount = Double.parseDouble(limitAmountField.getText());
            String period = limitPeriodComboBox.getValue();
            Database.setLimit(amount, period);
            showAlert("Zapisano", "Limit został zapisany.");
        } catch (NumberFormatException e) {
            showAlert("Błąd", "Nieprawidłowa kwota.");
        }
    }

    /**
     * Pokazuje widok ustawień limitów.
     */
    @FXML
    private void showLimitSettings() {
        mainContentBox.setVisible(false);
        statsBox.setVisible(false);
        limitSettingsBox.setVisible(true);
    }

    /**
     * Pokazuje okno informacyjne.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Powrót do głównego widoku (formularz + tabela).
     */
    @FXML
    private void handleReturnToMain() {
        mainContentBox.setVisible(true);
        limitSettingsBox.setVisible(false);
        statsBox.setVisible(false);
    }

    /**
     * Wyświetla statystyki, dane sumaryczne i wykres wydatków.
     */
    @FXML
    private void showStats() {
        mainContentBox.setVisible(false);
        limitSettingsBox.setVisible(false);
        statsBox.setVisible(true);

        List<Transaction> list = getTransactions();
        double income = 0, expenses = 0;
        int total = list.size();
        Map<String, Double> categoryMap = new HashMap<>();

        for (Transaction t : list) {
            double amt = t.getAmount();
            if (amt > 0) income += amt;
            else expenses += Math.abs(amt);

            if (amt < 0) {
                categoryMap.merge(t.getCategory(), Math.abs(amt), Double::sum);
            }
        }

        double balance = income - expenses;
        statsBalance.setText(String.format("Bilans: %.2f PLN", balance));
        statsIncome.setText(String.format("Suma przychodów: %.2f PLN", income));
        statsExpenses.setText(String.format("Suma wydatków: %.2f PLN", expenses));
        statsCount.setText(String.format("Liczba transakcji: %d", total));

        // Wykres kołowy
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        categoryMap.forEach((cat, val) -> pieData.add(new PieChart.Data(cat, val)));
        expensePieChart.setData(pieData);

        // Lista kategorii obok wykresu
        categoryStatsBox.getChildren().clear();
        pieData.forEach(d -> {
            String text = String.format("%s: %.2f PLN", d.getName(), d.getPieValue());
            categoryStatsBox.getChildren().add(new Label(text));
        });
    }
}
