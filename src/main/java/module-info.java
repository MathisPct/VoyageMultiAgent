module TP1 {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.polytech.ui.controller to javafx.fxml;
    opens org.polytech.agent to javafx.base;
    exports org.polytech;
}