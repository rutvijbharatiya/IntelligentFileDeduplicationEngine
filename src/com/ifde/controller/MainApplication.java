package com.ifde.controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        URL fxmlUrl = getClass().getResource("/com/ifde/view/MainView.fxml");

        if (fxmlUrl == null) {
            throw new RuntimeException(
                    "MainView.fxml not found: /com/ifde/view/MainView.fxml"
            );
        }

        Parent root = FXMLLoader.load(fxmlUrl);

        Scene scene = new Scene(root);

        URL iconUrl = getClass().getResource("/com/ifde/view/icon.png");

        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
        }

        stage.setTitle("Intelligent File Deduplication Engine");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}