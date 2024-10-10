package com.example.test_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistNFTordersController extends BaseController {

    @FXML
    private VBox ordersContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();

    @FXML
    public void initialize() {
        loadNFTOrders();
    }

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("ArtistNFTPageController: setUserId() called with userId: " + userId);
        loadNFTOrders();
    }

    private void loadNFTOrders() {
        ordersContainer.getChildren().clear();

        String query = "SELECT no.id, no.user_id, no.payment_address, no.amount, no.order_status, no.nft_id, " +
                "n.name AS nft_name, u.name AS customer_name " +
                "FROM nft_order no " +
                "JOIN nfts n ON no.nft_id = n.nft_id " +
                "JOIN users u ON no.user_id = u.user_id " +
                "WHERE n.artist_id = ? AND no.order_status = 'pending'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AnchorPane orderCard = createOrderCard(
                            rs.getInt("id"),
                            rs.getString("nft_name"),
                            rs.getString("customer_name"),
                            rs.getString("payment_address"),
                            rs.getString("order_status"),
                            rs.getInt("nft_id"),
                            rs.getInt("user_id"),
                            rs.getBigDecimal("amount")
                    );
                    ordersContainer.getChildren().add(orderCard);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load NFT orders: " + e.getMessage());
        }
    }

    private AnchorPane createOrderCard(int orderId, String nftName, String customerName, String paymentAddress, String status, int nftId, int customerId, BigDecimal amount) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(860, 150);
        card.setStyle("-fx-background-color: #F0F8FF; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Text nameText = new Text("NFT: " + nftName);
        nameText.setLayoutX(20);
        nameText.setLayoutY(30);
        nameText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Text customerText = new Text("Customer: " + customerName);
        customerText.setLayoutX(20);
        customerText.setLayoutY(60);

        Text addressText = new Text("Payment Address: " + paymentAddress);
        addressText.setLayoutX(20);
        addressText.setLayoutY(90);

        Text statusText = new Text("Status: " + status);
        statusText.setLayoutX(20);
        statusText.setLayoutY(120);

        Button confirmButton = new Button("Confirm");
        confirmButton.setLayoutX(700);
        confirmButton.setLayoutY(30);
        styleButton(confirmButton, "-fx-background-color: #4CAF50; -fx-text-fill: white;");
        confirmButton.setOnAction(e -> handleConfirm(orderId, nftId, customerId, amount));

        Button declineButton = new Button("Decline");
        declineButton.setLayoutX(780);
        declineButton.setLayoutY(30);
        styleButton(declineButton, "-fx-background-color: #f44336; -fx-text-fill: white;");
        declineButton.setOnAction(e -> handleDecline(orderId, customerId));

        card.getChildren().addAll(nameText, customerText, addressText, statusText, confirmButton, declineButton);

        return card;
    }
    private void styleButton(Button button, String style) {
        button.setStyle(style + "-fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setStyle(style + "-fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand; -fx-opacity: 0.8;"));
        button.setOnMouseExited(e -> button.setStyle(style + "-fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;"));
    }

    private void handleConfirm(int orderId, int nftId, int customerId, BigDecimal amount) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Check NFT quantity
            String checkQuantityQuery = "SELECT quantity FROM nfts WHERE nft_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuantityQuery)) {
                pstmt.setInt(1, nftId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int quantity = rs.getInt("quantity");
                        if (quantity != 1) {
                            showAlert(Alert.AlertType.WARNING, "Cannot Confirm", "This NFT has already been sold. Please decline the order.");
                            return;
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "NFT not found.");
                        return;
                    }
                }
            }





            // Update nfts table
            String updateNftQuery = "UPDATE nfts SET owner_id = ?, quantity = 0 WHERE nft_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateNftQuery)) {
                pstmt.setInt(1, customerId);
                pstmt.setInt(2, nftId);
                pstmt.executeUpdate();
            }

            // Update nft_order table
            String updateOrderQuery = "UPDATE nft_order SET order_status = 'completed' WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateOrderQuery)) {
                pstmt.setInt(1, orderId);
                pstmt.executeUpdate();
            }

            // Insert notification
            String insertNotificationQuery = "INSERT INTO notifications (user_id, type, title, message) VALUES (?, 'NFT_PURCHASE', 'NFT Purchase Confirmed', ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertNotificationQuery)) {
                pstmt.setInt(1, customerId);
                pstmt.setString(2, "Your NFT purchase has been confirmed. Amount: $" + amount);
                pstmt.executeUpdate();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order confirmed successfully.");
            loadNFTOrders(); // Refresh the order list
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to confirm order: " + e.getMessage());
        }
    }

    private void handleDecline(int orderId, int customerId) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Update nft_order table
            String updateOrderQuery = "UPDATE nft_order SET order_status = 'failed' WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateOrderQuery)) {
                pstmt.setInt(1, orderId);
                pstmt.executeUpdate();
            }

            // Insert notification
            String insertNotificationQuery = "INSERT INTO notifications (user_id, type, title, message) VALUES (?, 'NFT_PURCHASE', 'NFT Purchase Declined', 'Your NFT purchase has been declined.')";
            try (PreparedStatement pstmt = conn.prepareStatement(insertNotificationQuery)) {
                pstmt.setInt(1, customerId);
                pstmt.executeUpdate();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order declined successfully.");
            loadNFTOrders(); // Refresh the order list
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to decline order: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Navigation methods (same as in ArtistPaintingPageController)
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
        System.out.println("ArtistNFTPageController: Logging out user");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/CustomerLoginPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        System.out.println("ArtistNFTPageController: Navigated to login page");
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

    @FXML
    void mynft(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNFTPage.fxml");
    }

    @FXML
    void nftorders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNFTorders.fxml");
    }

    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("ArtistNFTPageController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("ArtistNFTPageController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("ArtistNFTPageController: Warning - controller is null");
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