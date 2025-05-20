module com.financetracker.financetracker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens com.financetracker.financetracker to javafx.fxml;
    exports com.financetracker.financetracker;
}