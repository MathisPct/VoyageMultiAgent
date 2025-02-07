package org.polytech;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.application.Application;

import java.io.File;
import java.net.URL;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlFile = new File("src/main/resources/main.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(fxmlFile);
        Scene scene = new Scene(root);
        primaryStage.setTitle("VoyageMultiAgent");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(540);
        primaryStage.setMinWidth(540);
        primaryStage.show();
    }
}