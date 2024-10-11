package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class adminDashboardController extends  BaseController {
    @FXML
    public void initialize() {

        checkAndUpdateAuctions();
    }



    @FXML
    private Text artistCount;

    @FXML
    private Text artworkCount;

    @FXML
    private Text customerCount;

    @FXML
    private Text eventCount;

    @FXML
    private Text inqueriesCount;

    @FXML
    private Text totalSellCount;

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("ArtistDashboardController: userId set to " + userId);
        loadDashboardData();
    }

    public int getUserId() {
        return userId;
    }
    private void loadDashboardData() {
        try (Connection conn = DataBaseConnection.getConnection()) {
            loadArtistCount(conn);
            loadArtworkCount(conn);
            loadCustomerCount(conn);
            loadEventCount(conn);
            loadinqueriesCount(conn);
            loadRevenue(conn);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private void loadArtworkCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(painting_id) as Total_Paint FROM paintings";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Total_Paint");
                    artworkCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadCustomerCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(user_id) as Customer FROM users where role ='customer'";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Customer");
                    customerCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadEventCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(event_id) as Event FROM events ";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Event");
                    eventCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadinqueriesCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(contact_id) as q FROM contactus";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("q");
                    inqueriesCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadRevenue(Connection conn) throws SQLException {
        String query = "SELECT p.artist_id, SUM(oi.price * oi.quantity) AS Revenue\n" +
                "FROM orders o\n" +
                "JOIN orderitems oi ON o.order_id = oi.order_id\n" +
                "JOIN paintings p ON oi.painting_id = p.painting_id\n" +
                "WHERE o.order_status = 'active'";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Revenue");
                    totalSellCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadArtistCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(user_id) as Artist FROM users where role ='artist'";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Artist");
                    artistCount.setText(String.valueOf(count));
                }
            }
        }
    }



    //Auction checking and all staff


    private void checkAndUpdateAuctions() {
        try (Connection conn = DataBaseConnection.getConnection()) {

            String auctionQuery = "SELECT auction_id, painting_id, current_bid, ends_time, start_date, status FROM auctions";
            try (PreparedStatement pstmt = conn.prepareStatement(auctionQuery)) {
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int auctionId = rs.getInt("auction_id");
                    int paintingId = rs.getInt("painting_id");
                    BigDecimal currentBid = rs.getBigDecimal("current_bid");
                    Timestamp endsTime = rs.getTimestamp("ends_time");
                    Timestamp startDate = rs.getTimestamp("start_date");
                    String status = rs.getString("status");

                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime endTime = endsTime.toLocalDateTime();
                    LocalDateTime startTime = startDate.toLocalDateTime();

                    // Check if auction needs to be set to 'active'
                    if (now.isAfter(startTime) && now.isBefore(endTime) && status.equals("pending")) {
                        updateAuctionStatus(conn, auctionId, "active");
                    }

                    // Check if auction needs to be set to 'ended'
                    if (now.isAfter(endTime) && !status.equals("ended")) {
                        endAuction(conn, auctionId, paintingId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateAuctionStatus(Connection conn, int auctionId, String status) throws SQLException {
        String updateQuery = "UPDATE auctions SET status = ? WHERE auction_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, auctionId);
            pstmt.executeUpdate();
        }
    }

    private void endAuction(Connection conn, int auctionId, int paintingId) throws SQLException {
        // Get the highest bid
        String bidQuery = "SELECT user_id, bid_amount FROM bids WHERE auction_id = ? ORDER BY bid_amount DESC LIMIT 1";
        try (PreparedStatement bidStmt = conn.prepareStatement(bidQuery)) {
            bidStmt.setInt(1, auctionId);
            ResultSet bidRs = bidStmt.executeQuery();
            if (bidRs.next()) {
                int winningUserId = bidRs.getInt("user_id");
                BigDecimal winningBid = bidRs.getBigDecimal("bid_amount");

                // Find  artist id
                int artistId = getArtistByPaintingId(conn, paintingId);

                // Retrieve the bidder's email
                String bidderEmail = getBidderEmail(conn, winningUserId);

                // Transfer credits from  winning bidder to  artist
                transferCredits(conn, winningUserId, artistId, winningBid);

                // Create a notification for the winning bidder
                createNotification(conn, winningUserId, "Auction Won", "Congratulations! You've won the auction for painting ID " + paintingId + ". Amount: " + winningBid);


                // Create a notification for the artist with bidder's email
                String artistMessage = "Your painting has been sold in the auction. Amount credited: " + winningBid +
                        ". Please contact the winning bidder at: " + bidderEmail +
                        " for the delivery of the painting.";
                createNotification(conn, artistId, "Auction Ended", artistMessage);

                // Set the auction status to 'ended'
                updateAuctionStatus(conn, auctionId, "ended");
            }
        }
    }

    private String getBidderEmail(Connection conn, int WinuserId) throws SQLException {
        String emailQuery = "SELECT email FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(emailQuery)) {
            pstmt.setInt(1, WinuserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        }
        return null;
    }

    private int getArtistByPaintingId(Connection conn, int paintingId) throws SQLException {
        String query = "SELECT artist_id FROM paintings WHERE painting_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, paintingId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("artist_id");
            }
        }
        return -1; // Default return value in case the artist is not found
    }

    private void transferCredits(Connection conn, int fromUserId, int toUserId, BigDecimal amount) throws SQLException {
        // Deduct amount from the winning bidder
        String deductQuery = "UPDATE users SET credits = credits - ? WHERE user_id = ?";
        try (PreparedStatement deductStmt = conn.prepareStatement(deductQuery)) {
            deductStmt.setBigDecimal(1, amount);
            deductStmt.setInt(2, fromUserId);
            deductStmt.executeUpdate();
        }

        // Credit amount to the artist
        String creditQuery = "UPDATE users SET credits = credits + ? WHERE user_id = ?";
        try (PreparedStatement creditStmt = conn.prepareStatement(creditQuery)) {
            creditStmt.setBigDecimal(1, amount);
            creditStmt.setInt(2, toUserId);
            creditStmt.executeUpdate();
        }
    }

    private void createNotification(Connection conn, int xuser, String title, String message) throws SQLException {
        String notificationQuery = "INSERT INTO notifications (user_id, title, message, created_at) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(notificationQuery)) {
            pstmt.setInt(1, xuser);
            pstmt.setString(2, title);
            pstmt.setString(3, message);
            pstmt.executeUpdate();
        }
    }




    @FXML
    void addArtist(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddArtist.fxml");
    }

    @FXML
    void addArtworks(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddArtwork.fxml");
    }

    @FXML
    void addEvent(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddEvent.fxml");

    }

    @FXML
    void goAuctionRequest(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAuctionPage.fxml");
    }

    @FXML
    void goDashboard(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminDashboard.fxml");

    }

    @FXML
    void goLogout(ActionEvent event) throws IOException {

        loadPage(event, "Customer/customerLoginPage.fxml");



    }

    @FXML
    void addCustomer(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddCustomers.fxml");

    }

    @FXML
    void manageCustomer(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageCustomers.fxml");

    }


    @FXML
    void goMessages(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminMessages.fxml");


    }

    @FXML
    void goOrders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminOrderPage.fxml");

    }

    @FXML
    void goQueries(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminQueriesPage.fxml");

    }

    @FXML
    void manageArtist(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageArtist.fxml");

    }

    @FXML
    void manageArtwork(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageArtwork.fxml");


    }

    @FXML
    void manageEvent(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageEvent.fxml");

    }

    @FXML
    void goCreditRequest(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminCreditRequest.fxml");

    }
    @FXML
    void goWithdrawRequest(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminWithdrawRequest.fxml");

    }



    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("AdminDashboardController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("AdminDashboardController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("AdminDashboardController: Warning - controller is null");
        }

        Scene scene = new Scene(root);
        Stage stage;

        if (event.getSource() instanceof MenuItem) {
            // If the event source is a MenuItem, we need to get the stage differently
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
