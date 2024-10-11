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
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ArtistNotificationController extends BaseController {

    @FXML
    private VBox notificationContainer;

    @FXML
    private Button markAllReadBtn;

    private DataBaseConnection dbConnection = new DataBaseConnection();


    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("CustomerNotificationController: userId set to " + userId);
        generateNotifications();
        loadNotifications();
    }




    private void generateNotifications() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Check for auctions ending tomorrow
        String auctionQuery = "SELECT a.*, p.name FROM auctions a JOIN paintings p ON a.painting_id = p.painting_id LEFT JOIN notifications n ON n.type = 'auction' AND n.user_id = ? AND n.message LIKE CONCAT('%', p.name, '%') WHERE DATE(a.ends_time) = ? AND a.status = 'active' AND n.id IS NULL";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(auctionQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, tomorrow.format(formatter));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                createNotification("auction", "Auction Ending Tomorrow: " + rs.getString("name"),
                        "The auction for '" + rs.getString("name") + "' is ending tomorrow. Current bid: $" + rs.getDouble("current_bid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Check for user birthdays tomorrow
        String birthdayQuery = "SELECT u.* FROM users u LEFT JOIN notifications n ON n.type = 'birthday' AND n.user_id = ? AND n.message LIKE CONCAT('%', u.name, '%') AND YEAR(n.created_at) = YEAR(CURDATE()) WHERE DATE_FORMAT(u.dob, '%m-%d') = DATE_FORMAT(?, '%m-%d') AND n.id IS NULL";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(birthdayQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, tomorrow.format(formatter));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                createNotification("birthday", "Birthday Reminder",
                        "Tomorrow is " + rs.getString("name") + "'s birthday!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // auction ended
        String auctionEndedQuery = "SELECT a.*, p.name, b.user_id AS winner_id, u.name AS winner_name, b.bid_amount FROM auctions a JOIN paintings p ON a.painting_id = p.painting_id LEFT JOIN bids b ON a.auction_id = b.auction_id LEFT JOIN users u ON b.user_id = u.user_id LEFT JOIN notifications n ON n.type = 'auction_ended' AND n.user_id = ? AND n.message LIKE CONCAT('%', p.name, '%') WHERE a.ends_time < ? AND a.status = 'active' AND n.id IS NULL ORDER BY b.bid_amount DESC LIMIT 1";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(auctionEndedQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, now.format(formatter));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String winnerName = rs.getString("winner_name");
                double maxBid = rs.getDouble("bid_amount");
                createNotification("auction_ended", "Auction Ended: " + rs.getString("name"),
                        " Auction for '" + rs.getString("name") + "' has ended. " +
                                (winnerName != null ? "The winning bid was $" + maxBid + " by " + winnerName + "." : "There were no bids for this auction."));
                // Update auction status
                updateAuctionStatus(rs.getInt("auction_id"), "ended");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateAuctionStatus(int auctionId, String status) {
        String updateQuery = "UPDATE auctions SET status = ? WHERE auction_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, auctionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createNotification(String type, String title, String message) {
        String insertQuery = "INSERT INTO notifications (user_id, type, title, message, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            pstmt.setString(3, title);
            pstmt.setString(4, message);
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadNotifications() {
        notificationContainer.getChildren().clear();
        String query = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notificationContainer.getChildren().add(createNotificationCard(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("read_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Node createNotificationCard(int id, String title, String message, Timestamp createdAt, Timestamp readAt) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPrefWidth(350);
        card.setStyle("-fx-background-color: " + (readAt == null ? "#f0f8ff" : "#ffffff") + ";" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-width: 0 0 1 0;" +
                "-fx-padding: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        // Title
        Text titleText = new Text(title);
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Message
        Text messageText = new Text(message);
        messageText.setWrappingWidth(300);

        // Date
        Text dateText = new Text(createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm")));
        dateText.setStyle("-fx-fill: #888888; -fx-font-size: 12px;");

        // Buttons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button markReadBtn = new Button(readAt == null ? "Mark as Read" : "Mark as Unread");
        markReadBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        markReadBtn.setOnAction(e -> toggleReadStatus(id, readAt == null));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteNotification(id));

        buttonBox.getChildren().addAll(markReadBtn, deleteBtn);

        card.getChildren().addAll(titleText, messageText, dateText, buttonBox);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-background-color: #f5f5f5;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle() + "-fx-background-color: " + (readAt == null ? "#f0f8ff" : "#ffffff") + ";"));

        return card;
    }

    private void toggleReadStatus(int notificationId, boolean markAsRead) {
        String updateQuery = markAsRead ?
                "UPDATE notifications SET read_at = ? WHERE id = ?" :
                "UPDATE notifications SET read_at = NULL WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            if (markAsRead) {
                pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(2, notificationId);
            } else {
                pstmt.setInt(1, notificationId);
            }
            pstmt.executeUpdate();
            loadNotifications();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void markAllAsRead(ActionEvent event) {
        String updateQuery = "UPDATE notifications SET read_at = ? WHERE user_id = ? AND read_at IS NULL";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Marked " + affectedRows + " notifications as read");
            loadNotifications();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void markAsRead(int notificationId) {
        String updateQuery = "UPDATE notifications SET read_at = ? WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, notificationId);
            pstmt.executeUpdate();
            loadNotifications();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteNotification(int notificationId) {
        String deleteQuery = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
            loadNotifications();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    @FXML
    void mynft(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNFTPage.fxml");
    }


    @FXML
    void nftorders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNFTorders.fxml");

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