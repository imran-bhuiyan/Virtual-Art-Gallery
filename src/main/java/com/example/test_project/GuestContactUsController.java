package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GuestContactUsController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField messageField;

    private DataBaseConnection dbConnection;

    public GuestContactUsController() {
        dbConnection = new DataBaseConnection();
    }

    @FXML
    private void goHome(ActionEvent event) throws IOException {
        loadPage(event, "guest/userOrGuestHomePage.fxml");
    }

    @FXML
    private void goEvents(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestEventPage.fxml");
    }

    @FXML
    private void goPaintings(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestPaintingsPage.fxml");
    }

    @FXML
    private void goTopArt(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestTopArtPage.fxml");
    }

    @FXML
    private void goContactUs(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestContactUsPage.fxml");
    }

    @FXML
    private void goLogin(ActionEvent event) throws IOException {
        loadPage(event, "customer/customerLoginPage.fxml");
    }

    @FXML
    private void submitContactForm(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String message = messageField.getText();

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (saveToDatabase(name, email, message)) {
            showSweetAlert("Success", "Your message has been submitted successfully!", event);
        } else {
            showAlert("Error", "Failed to submit your message. Please try again.");
        }
    }

    private boolean saveToDatabase(String name, String email, String message) {
        String sql = "INSERT INTO contactus (name, email, message) VALUES (?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, message);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSweetAlert(String title, String content, ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait().ifPresent(rs -> {
            if (rs == ButtonType.OK) {
                try {
                    loadPage(event, "guest/guestContactUsPage.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}