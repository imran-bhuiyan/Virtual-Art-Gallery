package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class userRegistrationController {

    @FXML
    private TextField userName;

    @FXML
    private TextField uemail;

    @FXML
    private PasswordField upass;

    @FXML
    private ComboBox<String> userRole;

    @FXML
    private DatePicker birthday;

    private DataBaseConnection dbConnection;

    public void initialize() {
        dbConnection = new DataBaseConnection();
    }

    @FXML
    private void registerUser(ActionEvent event) {
        String name = userName.getText();
        String email = uemail.getText();
        String password = upass.getText();
        String role = userRole.getValue();
        LocalDate dob = birthday.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null || dob == null) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please enter a valid email address.");
            return;
        }

        if (isEmailUnique(email)) {
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO users (name, email, password, role, created_at, dob) VALUES (?, ?, ?, ?, ?, ?)")) {

                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, password); //
                pstmt.setString(4, role);
                pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                pstmt.setDate(6, Date.valueOf(dob));

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You have been successfully registered!");
                    goToLoginPage();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Registration Error", "Failed to register. Please try again.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while registering.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Email already exists. Please use a different email.");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private boolean isEmailUnique(String email) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?")) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goHome(ActionEvent event) {
        // Implement navigation to home page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("guest/UserOrGuestHomePage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load the home page.");
        }
    }

    private void goToLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/customerLoginPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load the login page.");
        }
    }
}