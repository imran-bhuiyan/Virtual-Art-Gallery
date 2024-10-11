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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistDashboardController extends BaseController{
    private int userId;

    @FXML
    private Text artworkCount;

    @FXML
    private Text revenueCount;

    @FXML
    private Text soldItemsCount;

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
            loadArtworksCount(conn);
            loadSoldItemsCount(conn);
            loadRevenue(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private void loadArtworksCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(painting_id) as Total_Paint FROM paintings WHERE artist_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Total_Paint");
                    artworkCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadSoldItemsCount(Connection conn) throws SQLException {
        String query = "SELECT p.artist_id, \n" +
                "       SUM(oi.quantity) AS Sold\n" +
                "FROM orders o\n" +
                "JOIN orderitems oi ON o.order_id = oi.order_id\n" +
                "JOIN paintings p ON oi.painting_id = p.painting_id\n" +
                "WHERE o.order_status = 'active' and p.artist_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Sold");
                    soldItemsCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadRevenue(Connection conn) throws SQLException {
        String query = "SELECT p.artist_id, SUM(oi.price * oi.quantity) AS Revenue\n" +
                "FROM orders o\n" +
                "JOIN orderitems oi ON o.order_id = oi.order_id\n" +
                "JOIN paintings p ON oi.painting_id = p.painting_id\n" +
                "WHERE o.order_status = 'active' and p.artist_id = ? ";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double totalRevenue = rs.getDouble("Revenue");
                    revenueCount.setText(String.format("%.2f", totalRevenue));
                }
            }
        }
    }

// Navigate fxml files

    @FXML
    void artistAddAuctions(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddAuction.fxml");
    }

    @FXML
    void artistAddPainting(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddPaint.fxml");
    }

    @FXML
    void artistDashboard(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistDashboard.fxml");


    }

    @FXML
    void artistLogout(ActionEvent event) throws IOException {
        System.out.println("ArtistPaintingPageController: Logging out user");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/CustomerLoginPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        System.out.println("ArtistPaintingPageController: Navigated to login page");
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
    void artistAddNFTs(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddNFTs.fxml");

    }


    @FXML
    void artistSeeBalance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Artist/ArtistBalanceWindow.fxml"));
            Parent root = loader.load();

            ArtistBalanceController balanceController = loader.getController();
            balanceController.setUserId(this.userId);
            balanceController.loadBalance();

            Stage balanceStage = new Stage();
            balanceStage.setTitle("Artist Balance");
            balanceStage.setScene(new Scene(root));
            balanceStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("ArtistDashboardController: loadPageWithUserId() called with path: " + fxmlPath);
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
            // If it's a regular Node (like a Button), we can get the stage as before
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Event source not recognized");
        }

        stage.setScene(scene);
        stage.show();
    }


}