module TP1 {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.polytech.ui.controller to javafx.fxml;
    exports org.polytech;
}