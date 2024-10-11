package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class guestTopArtController {

    @FXML
    private Text titleText;

    @FXML
    private Text artistText;

    @FXML
    private Text reactionsText;

    @FXML
    private ImageView paintingImageView;

    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Image_For_Database";

    @FXML
    public void initialize() {
        loadTopArt();
    }

    private void loadTopArt() {

        String query = "SELECT p.name as Title, COUNT(r.painting_id) as Total_Reaction, u.name as Artist, p.image_url " +
                "FROM reactions as r JOIN paintings as p ON r.painting_id = p.painting_id " +
                "JOIN users as u ON u.user_id = p.artist_id " +
                "GROUP BY r.painting_id " +
                "ORDER BY Total_Reaction DESC " +
                "LIMIT 1";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                titleText.setText(rs.getString("Title") != null ? rs.getString("Title") : "N/A");
                artistText.setText(rs.getString("Artist") != null ? rs.getString("Artist") : "Unknown");
                reactionsText.setText(String.valueOf(rs.getInt("Total_Reaction")));

                String imageUrl = rs.getString("image_url");
                loadImage(imageUrl);
            } else {
                titleText.setText("No top art found");
                artistText.setText("N/A");
                reactionsText.setText("0");
                setPlaceholderImage();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            titleText.setText("Error loading top art");
            artistText.setText("N/A");
            reactionsText.setText("N/A");
            setPlaceholderImage();
        }
    }

    private void loadImage(String imageUrl) {
        File imageFile = new File(imageUrl);
        if (imageFile.exists()) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                paintingImageView.setImage(image);
                paintingImageView.setFitWidth(500);
                paintingImageView.setFitHeight(300);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
                setPlaceholderImage();
            }
        } else {
            System.out.println("Image file not found: " + imageUrl);
            setPlaceholderImage();
        }
    }

    private void setPlaceholderImage() {
        File placeholderFile = new File(IMAGE_DIRECTORY, "placeholder.png");
        if (placeholderFile.exists()) {
            paintingImageView.setImage(new Image(placeholderFile.toURI().toString()));
            paintingImageView.setFitWidth(500);
            paintingImageView.setFitHeight(300);
        } else {
            System.out.println("Placeholder image not found");
        }
    }

    @FXML
    private void goHome(ActionEvent event) throws IOException {
        loadPage(event, "guest/userOrGuestHomePage.fxml");
    }

    @FXML
    private void goEvents(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestEventPage.fxml");
    }

    @FXML
    private void goPaintings(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestPaintingsPage.fxml");
    }

    @FXML
    private void goTopArt(ActionEvent event) throws IOException {
        // Already on Top Art page, so no action needed
        loadPage(event, "guest/guestTopArtPage.fxml");
    }

    @FXML
    private void goContactUS(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestContactUsPage.fxml");
    }

    @FXML
    private void goLogin(ActionEvent event) throws IOException {
        loadPage(event, "customer/customerLoginPage.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}