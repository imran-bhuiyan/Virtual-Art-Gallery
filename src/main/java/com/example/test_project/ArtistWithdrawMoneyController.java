package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;

public class ArtistWithdrawMoneyController extends BaseController {

    @FXML
    private TextField accountNo;

    @FXML
    private TextField amount;

    @FXML
    private TextField paymentMethod;

    private DataBaseConnection dbConnection = new DataBaseConnection();



    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);


    }

    @FXML
    void clear(ActionEvent event) {
        accountNo.clear();
        amount.clear();
        paymentMethod.clear();
    }

    @FXML
    void requestWithdrawal(ActionEvent event) {
        if (validateFields()) {
            BigDecimal withdrawAmount = new BigDecimal(amount.getText());
            if (checkSufficientBalance(withdrawAmount)) {
                saveWithdrawalRequest(withdrawAmount);
                showAlert(Alert.AlertType.CONFIRMATION, "Withdrawal Request", "Your withdrawal request has been submitted successfully.");
                clear(null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Insufficient Balance", "You don't have sufficient balance for this withdrawal.");
            }
        }
    }

    private boolean validateFields() {
        if (accountNo.getText().isEmpty() || amount.getText().isEmpty() || paymentMethod.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled.");
            return false;
        }
        try {
            new BigDecimal(amount.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid number for the amount.");
            return false;
        }
        return true;
    }

    private boolean checkSufficientBalance(BigDecimal withdrawAmount) {
        String query = "SELECT credits FROM users WHERE user_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                BigDecimal balance = rs.getBigDecimal("credits");
                return balance.compareTo(withdrawAmount) >= 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error checking balance: " + e.getMessage());
        }
        return false;
    }

    private void saveWithdrawalRequest(BigDecimal withdrawAmount) {
        String query = "INSERT INTO withdraw_credit (user_id, amount, account_no, payment_method) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setBigDecimal(2, withdrawAmount);
            pstmt.setString(3, accountNo.getText());
            pstmt.setString(4, paymentMethod.getText());
            pstmt.executeUpdate();

            // Update user's balance
            updateUserBalance(withdrawAmount);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error saving withdrawal request: " + e.getMessage());
        }
    }

    private void updateUserBalance(BigDecimal withdrawAmount) {
        String query = "UPDATE users SET credits = credits - ? WHERE user_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBigDecimal(1, withdrawAmount);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating user balance: " + e.getMessage());
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