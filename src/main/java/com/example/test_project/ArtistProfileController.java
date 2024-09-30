package com.example.test_project;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistProfileController {

    @FXML private Text db;
    @FXML private Text em;
    @FXML private Text nam;

    public void initialize() {
        loadProfileData();
    }

    public void setArtistId(int artistId) {
        System.out.println("artisId From Artist Profile Controller : " + artistId);
        CurrentArtist.getInstance().setArtistId(artistId);
        loadProfileData();
    }

    public void loadProfileData() {
        int artistId = CurrentArtist.getInstance().getArtistId();
        String query = "SELECT name, dob, email FROM Users WHERE user_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String dob = rs.getString("dob");
                String email = rs.getString("email");

                // Debug logging
                System.out.println("Artist ID: " + artistId);
                System.out.println("Name: " + name);
                System.out.println("DOB: " + dob);
                System.out.println("Email: " + email);

                if (nam != null) nam.setText(name != null ? name : "Not provided");
                if (db != null) db.setText(dob != null ? dob : "Not provided");
                if (em != null) em.setText(email != null ? email : "Not provided");
            } else {
                System.out.println("No data found for artist ID: " + artistId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading profile data: " + e.getMessage());
        }
    }

    @FXML
    void editProfile(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistProfileEdit.fxml");
    }

    @FXML
    void changepass(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistPasswordChange.fxml");
    }

    // Navigation methods
    @FXML void artistAddAuctions(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddAuction.fxml");
    }

    @FXML void artistAddPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddPaint.fxml");
    }

    @FXML void artistDashboard(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistDashboard.fxml");
    }

    @FXML void artistLogout(ActionEvent event) throws IOException {
        loadPage(event, "Guest/userOrGuestHomePage.fxml");
    }

    @FXML void artistMessages(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMessage.fxml");
    }

    @FXML void artistMyPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMyPaintPage.fxml");
    }

    @FXML void artistMyProfile(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistProfile.fxml");
    }

    @FXML void artistPaintings(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistPaintingPage.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof ArtistProfileController) {
            ((ArtistProfileController) controller).setArtistId(CurrentArtist.getInstance().getArtistId());
        }

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}