package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistDashboardController {

    @FXML private Text artwork;
    @FXML private Text soldIteams;
    @FXML private Text revenue;

    public void initialize() {
        loadDashboardData();
    }

    public void setArtistId(int artistId) {
        System.out.println("artisId From ArtistP DashBoard Controller : " + artistId);
        CurrentArtist.getInstance().setArtistId(artistId);
        loadDashboardData();
    }

    private void loadDashboardData() {
        int artistId = CurrentArtist.getInstance().getArtistId();
        try (Connection conn = DataBaseConnection.getConnection()) {
            loadArtworkCount(conn);
            loadSoldItemsCount(conn);
            loadTotalRevenue(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load dashboard data");
        }
    }

    private void loadArtworkCount(Connection conn) throws SQLException {
        int artistId = CurrentArtist.getInstance().getArtistId();
        String query = "SELECT COUNT(*) as artwork_count FROM Paintings WHERE artist_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && artwork != null) {
                artwork.setText(String.valueOf(rs.getInt("artwork_count")));
            }
        }
    }

    private void loadSoldItemsCount(Connection conn) throws SQLException {
        int artistId = CurrentArtist.getInstance().getArtistId();
        String query = "SELECT COUNT(*) as sold_items_count FROM OrderItems oi " +
                "JOIN Paintings p ON oi.painting_id = p.painting_id " +
                "WHERE p.artist_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && soldIteams != null) {
                soldIteams.setText(String.valueOf(rs.getInt("sold_items_count")));
            }
        }
    }

    private void loadTotalRevenue(Connection conn) throws SQLException {
        int artistId = CurrentArtist.getInstance().getArtistId();
        String query = "SELECT COALESCE(SUM(oi.price * oi.quantity), 0) as total_revenue " +
                "FROM OrderItems oi " +
                "JOIN Paintings p ON oi.painting_id = p.painting_id " +
                "WHERE p.artist_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && revenue != null) {
                revenue.setText(String.format("$%.2f", rs.getDouble("total_revenue")));
            }
        }
    }

    @FXML
    void artistAddAuctions(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddAuction.fxml");
    }

    @FXML
    void artistAddPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddPaint.fxml");
    }

    @FXML
    void artistDashboard(ActionEvent event) throws IOException {
        // No need to load a new page, we're already on the dashboard
    }

    @FXML
    void artistLogout(ActionEvent event) throws IOException {
        loadPage(event, "Guest/userOrGuestHomePage.fxml");
    }

    @FXML
    void artistMessages(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMessage.fxml");
    }

    @FXML
    void artistMyPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMyPaintPage.fxml");
    }

    @FXML
    void artistMyProfile(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistProfile.fxml");
    }

    @FXML
    void artistPaintings(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistPaintingPage.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof ArtistPageController) {
            ((ArtistPageController) controller).setArtistId(CurrentArtist.getInstance().getArtistId());
        }

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}