package com.example.test_project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.math.BigDecimal;

public class CustomerNFTCheckoutController extends BaseController {

    @FXML
    private Label addressnft;

    @FXML
    private TextField senderAddress;

    @FXML
    private Button orderNowButton;

    private int nftId;
    private BigDecimal price;
    private String paymentAddress;

    private DataBaseConnection dbConnection = new DataBaseConnection();

    public void setNFTDetails(int nftId, BigDecimal price) {
        this.nftId = nftId;
        this.price = price;
        loadPaymentAddress();
    }

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("CustomerNFTCheckoutController: setUserId() called with userId: " + userId);
    }

    private void loadPaymentAddress() {
        String query = "SELECT payment_address FROM nfts WHERE nft_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, nftId);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                paymentAddress = rs.getString("payment_address");
                addressnft.setText(paymentAddress);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading payment address: " + e.getMessage());
        }
    }

    @FXML
    private void handleOrderNow() {
        if (senderAddress.getText().isEmpty()) {
            showAlert("Error", "Please enter sender address.");
            return;
        }

        if (saveOrder()) {
            showConfirmation("Order Placed", "Your order has been placed successfully. Please wait for confirmation.");
        } else {
            showAlert("Error", "Failed to place the order. Please try again.");
        }
    }

    private boolean saveOrder() {
        String query = "INSERT INTO nft_order (nft_id, user_id, payment_address, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, nftId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, senderAddress.getText());
            pstmt.setBigDecimal(4, price);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error saving order: " + e.getMessage());
            return false;
        }
    }

    private void showConfirmation(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                closeWindow();
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) orderNowButton.getScene().getWindow();
        stage.close();
    }
}