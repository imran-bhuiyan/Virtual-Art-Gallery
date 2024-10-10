package com.example.test_project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class CustomerNFTCheckoutController extends BaseController {

    @FXML
    private Label nftTitleLabel;

    @FXML
    private Label artistLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Button orderNowButton;

    private int nftId;
    private double price;

    private DataBaseConnection dbConnection = new DataBaseConnection();


    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("CustomerHomePageController: userId set to " + userId);

    }

    public void setNFTDetails(String title, String artist, String price, int nftId) {
        nftTitleLabel.setText(title);
        artistLabel.setText(artist);
        priceLabel.setText(price);
        this.nftId = nftId;
        this.price = Double.parseDouble(price.replace("$", ""));
    }

    @FXML
    private void handleOrderNow() {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Insert into Orders table
            String orderQuery = "INSERT INTO Orders (user_id, total_amount, order_date) VALUES (?, ?, ?)";
            try (PreparedStatement orderStmt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, userId);
                orderStmt.setDouble(2, price);
                orderStmt.setObject(3, LocalDateTime.now());
                orderStmt.executeUpdate();

                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);

                        // Insert into OrderItems table
                        String itemQuery = "INSERT INTO OrderItems (order_id, nft_id, quantity, price) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement itemStmt = conn.prepareStatement(itemQuery)) {
                            itemStmt.setInt(1, orderId);
                            itemStmt.setInt(2, nftId);
                            itemStmt.setInt(3, 1); // Quantity is always 1 for NFTs
                            itemStmt.setDouble(4, price);
                            itemStmt.executeUpdate();
                        }
                    } else {
                        throw new SQLException("Creating order failed, no ID obtained.");
                    }
                }
            }

            conn.commit();
            System.out.println("Order placed successfully");

            // Close the checkout window
            Stage stage = (Stage) orderNowButton.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error placing order: " + e.getMessage());
        }
    }
}