package com.financetracker.financetracker;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MainController {

    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ListView<String> transactionList;
    @FXML private Label balanceLabel;

    private double balance = 0.0;

    @FXML
    private void handleAddTransaction() {
        String desc = descriptionField.getText();
        double amount = Double.parseDouble(amountField.getText());

        balance += amount;
        transactionList.getItems().add(desc + " (" + amount + ")");
        balanceLabel.setText("Saldo: " + String.format("%.2f", balance));

        descriptionField.clear();
        amountField.clear();
    }
}
