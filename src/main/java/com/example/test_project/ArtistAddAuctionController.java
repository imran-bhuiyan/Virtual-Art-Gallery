package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javafx.stage.Stage;

import java.io.IOException;

public class ArtistAddAuctionController extends BaseController {

    @FXML
    private TextField artId;

    @FXML
    private TextField basePrice;

    @FXML
    private DatePicker date;
    @FXML
    private DatePicker startDate;

    private int userId;

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("ArtistAddAuctionController: userId set to " + userId);
    }

    @FXML
    void addToDatabase(ActionEvent event) {
        String artIdStr = artId.getText();
        System.out.println(artIdStr);

        String basePriceStr = basePrice.getText();
        LocalDate endDate = date.getValue();
        LocalDate start = startDate.getValue();

        if (artIdStr.isEmpty() || basePriceStr.isEmpty() || endDate == null || start == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please Select all field");
            return;
        }

        // Here I have to add date exceptions. Like start date should be greater from current date and end date should be greater that start date.
        // Date validation logic
        LocalDate currentDate = LocalDate.now();
        if (start.isBefore(currentDate)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Start date must be today or later.");
            return;
        }

        if (endDate.isBefore(start)) {
            showAlert(Alert.AlertType.ERROR, "Error", "End date must be later than start date.");
            return;
        }

        try {
            int artIdInt = Integer.parseInt(artIdStr);
            double basePriceDouble = Double.parseDouble(basePriceStr);

            if (isArtistsPainting(artIdInt,userId)) {
                addAuction(artIdInt, basePriceDouble, endDate ,start);
            }


            else {
                showAlert(Alert.AlertType.ERROR, "Error", "This painting does not belong to you.");

            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Art ID or Base Price. Please enter valid numbers.");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error");
        }
    }

    private boolean isArtistsPainting(int artId,int userId) throws SQLException {
        String query = "SELECT painting_id FROM paintings WHERE artist_id = ? AND painting_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, artId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void addAuction(int artId, double basePrice, LocalDate endDate ,LocalDate start) throws SQLException {
        String query = "INSERT INTO auctions (painting_id, starting_bid, ends_time,start_date) VALUES (?, ?, ?,?)";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, artId);
            pstmt.setDouble(2, basePrice);
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));
            pstmt.setDate(4, java.sql.Date.valueOf(start));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.CONFIRMATION, "Success", "Auction Added Successfully");
                clear(null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed");
            }
        }
    }

    @FXML
    void clear(ActionEvent event) {
        artId.clear();
        basePrice.clear();
        date.setValue(null);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
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