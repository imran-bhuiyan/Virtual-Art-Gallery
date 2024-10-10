package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class ArtistOrdersController extends BaseController {

    @FXML
    private VBox ordersContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();

    @FXML
    public void initialize() {
        loadOrders();
    }
    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("ArtistPaintingPageController: setUserId() called with userId: " + userId);
        loadOrders();
    }

    private void loadOrders() {
        ordersContainer.getChildren().clear();
        String query = "SELECT o.order_id, GROUP_CONCAT(p.name ORDER BY p.painting_id SEPARATOR ', ') AS painting_names, " +
                "o.total_amount, o.order_status, o.user_id, u.email " +
                "FROM orders o " +
                "JOIN orderitems ot ON o.order_id = ot.order_id " +
                "JOIN paintings p ON p.painting_id = ot.painting_id " +
                "JOIN users u ON o.user_id = u.user_id " +  // Joining users table to get email
                "WHERE p.artist_id = ? AND o.order_status = 'pending' " +
                "GROUP BY o.order_id";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ordersContainer.getChildren().add(createOrderCard(
                        rs.getInt("order_id"),
                        rs.getString("painting_names"),
                        rs.getDouble("total_amount"),
                        rs.getString("order_status"),
                        rs.getInt("user_id"),
                        rs.getString("email")  // Fetching email
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load orders. Please try again.");
        }
    }

    private Node createOrderCard(int orderId, String paintingNames, double totalAmount, String orderStatus, int customerId, String email) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPrefWidth(350);
        card.setStyle("-fx-background-color: #ffffff;" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        // Order details
        Text orderIdText = new Text("Order ID: " + orderId);
        orderIdText.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Text paintingsText = new Text("Paintings: " + paintingNames);
        paintingsText.setWrappingWidth(300);

        Text amountText = new Text("Total Amount: $" + String.format("%.2f", totalAmount));
        Text statusText = new Text("Status: " + orderStatus);

        // Email in bold
        Text emailText = new Text("Customer Email: " + email);
        emailText.setStyle("-fx-font-weight: bold;");

        // New label in bold, italic, and red color
        Text contactText = new Text("Contact " + email + " to take details for delivery.");
        contactText.setStyle("-fx-font-weight: bold; -fx-font-style: italic; -fx-fill: red;");

        VBox detailsBox = new VBox(5, orderIdText, paintingsText, amountText, statusText, emailText, contactText);

        // Buttons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);

        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        confirmButton.setOnAction(e -> handleOrderConfirmation(orderId, totalAmount, customerId));

        Button declineButton = new Button("Decline");
        declineButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        declineButton.setOnAction(e -> handleOrderDecline(orderId, customerId));

        buttonBox.getChildren().addAll(confirmButton, declineButton);

        // Add details and buttons to the card
        card.getChildren().addAll(detailsBox, buttonBox);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-background-color: #f5f5f5;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle() + "-fx-background-color: #ffffff;"));

        return card;
    }

    private void handleOrderConfirmation(int orderId, double totalAmount, int customerId) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Check user's credit
            String creditQuery = "SELECT credits FROM users WHERE user_id = ?";
            try (PreparedStatement creditStmt = conn.prepareStatement(creditQuery)) {
                creditStmt.setInt(1, customerId);
                ResultSet rs = creditStmt.executeQuery();
                if (rs.next()) {
                    double userCredit = rs.getDouble("credits");
                    if (userCredit < totalAmount) {
                        showAlert("Insufficient Credit", "The customer does not have enough credit to complete this order.");
                        return;
                    }
                }
            }

            // Update order status
            String updateOrderQuery = "UPDATE orders SET order_status = 'active' WHERE order_id = ?";
            try (PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderQuery)) {
                updateOrderStmt.setInt(1, orderId);
                updateOrderStmt.executeUpdate();
            }

            // Deduct credit from user
            String deductCreditQuery = "UPDATE users SET credits = credits - ? WHERE user_id = ?";
            try (PreparedStatement deductCreditStmt = conn.prepareStatement(deductCreditQuery)) {
                deductCreditStmt.setDouble(1, totalAmount);
                deductCreditStmt.setInt(2, customerId);
                deductCreditStmt.executeUpdate();
            }

            // Add credit to artist
            String addCreditQuery = "UPDATE users SET credits = credits + ? WHERE user_id = ?";
            try (PreparedStatement addCreditStmt = conn.prepareStatement(addCreditQuery)) {
                addCreditStmt.setDouble(1, totalAmount);
                addCreditStmt.setInt(2, userId);
                addCreditStmt.executeUpdate();
            }

            // Create notification
            String notificationQuery = "INSERT INTO notifications (user_id, type, title, message, created_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement notificationStmt = conn.prepareStatement(notificationQuery)) {
                notificationStmt.setInt(1, customerId);
                notificationStmt.setString(2, "order_confirmation");
                notificationStmt.setString(3, "Order Confirmed");
                notificationStmt.setString(4, "Your order (ID: " + orderId + ") has been confirmed by the artist and " + totalAmount + "credit has been transferred to Seller Account");
                notificationStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                notificationStmt.executeUpdate();
            }

            conn.commit();
            showAlert("Success", "Order confirmed successfully.");
            loadOrders();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to confirm order. Please try again.");
        }
    }

    private void handleOrderDecline(int orderId, int customerId) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Update order status
            String updateOrderQuery = "UPDATE orders SET order_status = 'declined' WHERE order_id = ?";
            try (PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderQuery)) {
                updateOrderStmt.setInt(1, orderId);
                updateOrderStmt.executeUpdate();
            }

            // Create notification
            String notificationQuery = "INSERT INTO notifications (user_id, type, title, message, created_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement notificationStmt = conn.prepareStatement(notificationQuery)) {
                notificationStmt.setInt(1, customerId);
                notificationStmt.setString(2, "order_declined");
                notificationStmt.setString(3, "Order Declined");
                notificationStmt.setString(4, "Your order (ID: " + orderId + ") has been declined by the artist.");
                notificationStmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                notificationStmt.executeUpdate();
            }

            conn.commit();
            showAlert("Success", "Order declined successfully.");
            loadOrders();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to decline order. Please try again.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    // Navigation methods (same as in ArtistAddPaintController)
    @FXML
    void artistAddAuctions(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddAuction.fxml");
    }

    @FXML
    void artistAddNFTs(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddNFTs.fxml");
    }

    @FXML
    void artistDashboard(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistDashboard.fxml");
    }

    @FXML
    void artistLogout(ActionEvent event) throws IOException {
        System.out.println("ArtistAddNFTController: Logging out user");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/CustomerLoginPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        System.out.println("ArtistAddNFTController: Navigated to login page");
    }

    @FXML
    void artistMessages(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistMessage.fxml");
    }

    @FXML
    void artistMyPainting(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistMyPaintPage.fxml");
    }

    @FXML
    void artistMyProfile(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistProfile.fxml");
    }

    @FXML
    void artistNotification(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNotification.fxml");
    }

    @FXML
    void artistPaintings(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistPaintingPage.fxml");
    }

    @FXML
    void artistSeeAuction(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistSeeAuctionPage.fxml");
    }

    @FXML
    void artistOrders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistOrderPage.fxml");
    }


    @FXML
    void artistAddPainting(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddPaint.fxml");
    }

    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("ArtistAddNFTController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("ArtistAddNFTController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("ArtistAddNFTController: Warning - controller is null");
        }

        Scene scene = new Scene(root);
        Stage stage;

        if (event.getSource() instanceof MenuItem) {
            MenuItem menuItem = (MenuItem) event.getSource();
            stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
        } else if (event.getSource() instanceof Node) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Event source not recognized");
        }

        stage.setScene(scene);
        stage.show();
    }
}