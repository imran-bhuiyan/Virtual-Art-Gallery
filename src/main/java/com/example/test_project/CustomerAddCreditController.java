package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CustomerAddCreditController extends BaseController {

    @FXML
    private TextField credits;

    @FXML
    private TextField tnxId;

    @FXML
    private TextField contactInfo;

    public void setUserId(int userId) {
        this.userId = userId;

    }

    @FXML
    void submit(ActionEvent event) {
        String creditsStr = credits.getText();
        String transactionId = tnxId.getText();
        String contact = contactInfo.getText();

        if (creditsStr.isEmpty() || transactionId.isEmpty() || contact.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(creditsStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid amount. Please enter a valid number.");
            return;
        }

        String query = "INSERT INTO user_credit (user_id, amount, trnxId, contact, d_status) VALUES (?, ?, ?, ?, 'pending')";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, transactionId);
            pstmt.setString(4, contact);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Your credit request has been submitted. Please wait for approval.");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        ((Stage) credits.getScene().getWindow()).close();
                    }
                });
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No rows were affected. The credit request may not have been saved.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while submitting your request. Please try again. Error details: " + e.getMessage());
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}