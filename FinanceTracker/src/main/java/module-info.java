module com.financetracker.financetracker {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.financetracker.financetracker to javafx.fxml;
    exports com.financetracker.financetracker;
}
