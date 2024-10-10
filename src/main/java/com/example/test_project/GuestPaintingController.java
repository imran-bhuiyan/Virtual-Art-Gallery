package com.example.test_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class GuestPaintingController implements Initializable {

    @FXML
    private VBox paintingsContainer;

    @FXML
    private ComboBox<String> categoryComboBox;

    private DataBaseConnection dbConnection;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbConnection = new DataBaseConnection();
        setupCategoryFilter();
        loadPaintings("All Categories");
    }

    private void setupCategoryFilter() {
        categoryComboBox.getItems().addAll("All Categories", "Oil paint", "Acrylic paint", "Watercolor", "Gouache", "Tempera", "Encaustic", "Casein", "Alkyd");
        categoryComboBox.setValue("All Categories");
        categoryComboBox.setOnAction(this::handleCategoryChange);
    }

    @FXML
    private void handleCategoryChange(ActionEvent event) {
        String selectedCategory = categoryComboBox.getValue();
        loadPaintings(selectedCategory);
    }

    private void loadPaintings(String category) {
        paintingsContainer.getChildren().clear();

        String query = "SELECT p.name as Title, u.name as Artist, p.year, p.category, p.price, COUNT(r.painting_id) as Reaction " +
                "FROM paintings as p JOIN users as u ON p.artist_id = u.user_id " +
                "LEFT JOIN reactions as r ON r.painting_id = p.painting_id " +
                "WHERE p.category = ? OR ? = 'All Categories' " +
                "GROUP BY p.painting_id";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, category);
            pstmt.setString(2, category);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AnchorPane paintingCard = createPaintingCard(
                            rs.getString("Title"),
                            rs.getString("Artist"),
                            rs.getInt("year"),
                            rs.getString("category"),
                            rs.getDouble("price"),
                            rs.getInt("Reaction")
                    );
                    paintingsContainer.getChildren().add(paintingCard);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private AnchorPane createPaintingCard(String title, String artist, int year, String category, double price, int reactions) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(213.0);
        card.setPrefWidth(860.0);
        card.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Text titleText = new Text(220, 30, title);
        titleText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Text artistText = new Text(220, 57, "Artist: " + artist);
        Text yearText = new Text(221, 87, "Year: " + year);
        Text categoryText = new Text(220, 114, "Category: " + category);
        Text priceText = new Text(220, 142, "Price: $" + price);
        Text reactionsText = new Text(269, 170, String.valueOf(reactions));

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setLayoutX(750);
        addToCartButton.setLayoutY(10);
        addToCartButton.setStyle("-fx-background-color: #40E0D0;");

        Button likeButton = new Button();
        likeButton.setLayoutX(218);
        likeButton.setLayoutY(153);
        ImageView likeImageView = new ImageView(new Image("file:/D:/Trimester/8th/AOOP/IntellijIdea/Test_Project/src/main/java/Icon/Love.png"));
        likeImageView.setFitWidth(20);
        likeImageView.setFitHeight(20);
        likeButton.setGraphic(likeImageView);

        card.getChildren().addAll(titleText, artistText, yearText, categoryText, priceText, reactionsText, addToCartButton, likeButton);

        return card;
    }

    // Navigation methods (you can implement these as needed)
    @FXML
    private void goHome(ActionEvent event) throws IOException {
        loadPage(event,"guest/userOrGuestHomePage.fxml");
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