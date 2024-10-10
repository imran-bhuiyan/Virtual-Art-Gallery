package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class customerPageTopArtController extends BaseController {
    private int userId;

    @FXML
    private Text titleText;

    @FXML
    private Text artistText;

    @FXML
    private Text reactionsText;

    @FXML
    private ImageView paintingImageView;

    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Image_For_Database";

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("CustomerTopPageController: userId set to " + userId);
        loadTopArt(); // Load top art when userId is set
    }

    @FXML
    public void initialize() {
        // Initialize components if needed
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

    // Navigation methods
    @FXML
    void goCustomerHome(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerHomePage.fxml");
    }

    @FXML
    void goCustomerPaintings(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerPaintingPage.fxml");
    }

    @FXML
    void goCustomerTopArt(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerPageTopArt.fxml");
    }

    @FXML
    void goCustomerEvents(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerEventPage.fxml");
    }

    @FXML
    void goCustomerAuction(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerLiveAuctionPage.fxml");
    }

    @FXML
    void goCustomerNFT(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerNFTpage.fxml");
    }

    @FXML
    void goCustomerProfile(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerProfilePage.fxml");
    }

    @FXML
    void goCustomerCart(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerPaintingCheckout.fxml");
    }

    @FXML
    void goCustomerNotification(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerNotification.fxml");
    }

    @FXML
    void goCustomerMessages(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerMessengerPage.fxml");
    }

    @FXML
    void customerLogout(ActionEvent event) throws IOException {
        System.out.println("CustomerTopArtController: Logging out user");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/customerLoginPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        System.out.println("CustomerTopArtController: Navigated to login page");
    }
    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("ArtistDashboardController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("CustomerHomePageController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("CustomerHomePageController: Warning - controller is null");
        }

        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

}
