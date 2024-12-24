package com.example.dbproject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField jdbcUrlField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    protected void connectToDatabase() {
        statusLabel.setText("");

        try {
            if (jdbcUrlField.getText().isEmpty() || usernameField.getText().isEmpty()) {
                statusLabel.setText("Please fill in all connection details");
                return;
            }

            String url = jdbcUrlField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();

            Connection connection = DriverManager.getConnection(url, username, password);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-browser-view.fxml"));
            Parent root = loader.load();

            MainController browserController = loader.getController();
            browserController.setConnection(connection);

            Scene scene = new Scene(root);
            Stage stage = (Stage) jdbcUrlField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Database Search Project Browser Page");
            stage.show();

        } catch (SQLException e) {
            showError("Connection failed", e.getMessage());
        } catch (IOException e) {
            showError("Loading Error", "Error loading main browser view: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        statusLabel.setText(message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}