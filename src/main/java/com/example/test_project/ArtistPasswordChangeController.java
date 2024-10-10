package com.example.test_project;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistPasswordChangeController extends BaseController {

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    private String currentPasswordFromDB;

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        loadCurrentPassword(); // Load the current password when the user opens the page
    }

    private void loadCurrentPassword() {
        try (Connection conn = DataBaseConnection.getConnection()) {
            String query = "SELECT password FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentPasswordFromDB = rs.getString("password"); // Assuming password is stored as plain text (you should use hashed passwords)
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to load user password.");
        }
    }

    @FXML
    private void handleChangePassword() {
        String currentPasswordInput = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!validatePasswords(currentPasswordInput, newPassword, confirmPassword)) {
            return;
        }

        // Logic for updating the password in the database
        if (updatePasswordInDatabase(newPassword)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully.");
            clearFields();
        }
    }

    private boolean validatePasswords(String currentPasswordInput, String newPassword, String confirmPassword) {
        // Check if fields are filled
        if (currentPasswordInput.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
            return false;
        }

        // Check if current password matches the one stored in the database
        if (!currentPasswordInput.equals(currentPasswordFromDB)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect.");
            return false;
        }

        // Check if the new password is the same as the current password
        if (newPassword.equals(currentPasswordFromDB)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New password cannot be the same as the current password.");
            return false;
        }

        // Check if new password and confirm password match
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New password and confirmation do not match.");
            return false;
        }

        return true;
    }

    private boolean updatePasswordInDatabase(String newPassword) {
        try (Connection conn = DataBaseConnection.getConnection()) {
            String query = "UPDATE users SET password = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, newPassword); // Password should be hashed before storing
                pstmt.setInt(2, userId);
                int rowsUpdated = pstmt.executeUpdate();
                return rowsUpdated > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update the password.");
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
}
