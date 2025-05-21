package com.financetracker.financetracker;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MainController {

    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ListView<Transaction> transactionList;
    @FXML private Label balanceLabel;

    private double balance = 0.0;

    @FXML
    public void initialize() {
        categoryComboBox.getItems().addAll(
                "Jedzenie", "Transport", "Rozrywka", "Zakupy", "Inne"
        );
    }

    @FXML
    private void handleAddTransaction() {
        String desc = descriptionField.getText();
        String cat = categoryComboBox.getValue();
        double amount;

        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            showAlert("Nieprawidłowa kwota.");
            return;
        }

        if (desc.isEmpty() || cat == null) {
            showAlert("Wszystkie pola muszą być wypełnione.");
            return;
        }

        Transaction t = new Transaction(desc, amount, cat);
        balance += amount;
        transactionList.getItems().add(t);
        balanceLabel.setText("Saldo: " + String.format("%.2f zł", balance));

        descriptionField.clear();
        amountField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}