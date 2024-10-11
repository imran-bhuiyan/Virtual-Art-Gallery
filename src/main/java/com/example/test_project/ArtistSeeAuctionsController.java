package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistSeeAuctionsController extends BaseController {

    @FXML
    private FlowPane auctionCardsPane;

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("ArtistDashboardController: userId set to " + userId);
        loadAuctionData();
    }


    public void initialize() {
        System.out.println("ArtistSeeAuctionsController: Initializing...");
        loadAuctionData();
    }

    private void loadAuctionData() {
        System.out.println("ArtistSeeAuctionsController: Loading auction data...");
        String query = "SELECT p.name, u.name as Artist_Name, a.auction_id, a.current_bid, a.ends_time, a.status " +
                "FROM auctions a " +
                "JOIN paintings p ON a.painting_id = p.painting_id " +
                "JOIN users u ON u.user_id = p.artist_id " +
                "WHERE p.artist_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            System.out.println("ArtistSeeAuctionsController: Executing query for user ID: " + userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    String paintingName = rs.getString("name");
                    String artistName = rs.getString("Artist_Name");
                    int auctionId = rs.getInt("auction_id");
                    double currentBid = rs.getDouble("current_bid");
                    String endsTime = rs.getTimestamp("ends_time").toString();
                    String status = rs.getString("status");

                    VBox card = createAuctionCard(artistName, auctionId, paintingName, currentBid, endsTime, status);
                    auctionCardsPane.getChildren().add(card);
                    count++;
                }

                System.out.println("ArtistSeeAuctionsController: Total auctions retrieved: " + count);
            }
        } catch (SQLException e) {
            System.err.println("ArtistSeeAuctionsController: SQL Exception occurred:");
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private VBox createAuctionCard(String artistName, int auctionId, String paintingName,
                                   double currentBid, String endsTime, String status) {
        System.out.println("ArtistSeeAuctionsController: Creating auction card for painting: " + paintingName);

        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #F0F8FF; -fx-border-color: #40E0D0; -fx-border-width: 2; -fx-padding: 15; -fx-border-radius: 5;");
        card.setPrefWidth(300);

        Label titleLabel = new Label(paintingName);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #333;");

        Label artistLabel = new Label("Artist: " + artistName);
        artistLabel.setStyle("-fx-text-fill: #666;");

        Label auctionIdLabel = new Label("Auction ID: " + auctionId);
        auctionIdLabel.setStyle("-fx-text-fill: #999;");

        Label bidLabel = new Label(String.format("Current Bid: $%.2f", currentBid));
        bidLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #008000;");

        Label timeLabel = new Label("Ends: " + endsTime);
        timeLabel.setStyle("-fx-text-fill: #FF4500;");

        Label statusLabel = new Label("Status: " + status);
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #B22222;");

        card.getChildren().addAll(titleLabel, artistLabel, auctionIdLabel, bidLabel, timeLabel, statusLabel);

        return card;
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