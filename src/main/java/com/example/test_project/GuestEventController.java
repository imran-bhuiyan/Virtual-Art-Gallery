package com.example.test_project;

import javafx.event.ActionEvent;
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
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class GuestEventController implements Initializable {

    @FXML
    private VBox eventContainer;

    private DataBaseConnection dbConnection;
    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Uploads";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DataBaseConnection();
        loadEventsFromDatabase();
    }

    private void loadEventsFromDatabase() {
        String query = "SELECT * FROM events";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                AnchorPane eventCard = createEventCard(
                        rs.getInt("event_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("date_time"),
                        rs.getString("venue"),
                        rs.getString("entry_fee"),
                        rs.getString("description"),
                        rs.getInt("capacity"),
                        rs.getString("picture_url")
                );
                eventContainer.getChildren().add(eventCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database Error .Failed to load events from database.");
        }
    }

    private AnchorPane createEventCard(int eventId, String name, String category, String dateTime, String venue, String entryFee, String description, int capacity, String imageUrl) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(245);
        card.setPrefWidth(860);
        card.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Text eventName = new Text(name);
        eventName.setLayoutX(220);
        eventName.setLayoutY(30);
        eventName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Text categoryText = new Text("Category: " + category);
        categoryText.setLayoutX(220);
        categoryText.setLayoutY(60);

        Text dateTimeText = new Text("Date: " + dateTime);
        dateTimeText.setLayoutX(220);
        dateTimeText.setLayoutY(85);

        Text venueText = new Text("Venue: " + venue);
        venueText.setLayoutX(220);
        venueText.setLayoutY(108);

        Text entryFeeText = new Text("Entry Fee: " + entryFee);
        entryFeeText.setLayoutX(220);
        entryFeeText.setLayoutY(134);

        Text capacityText = new Text("Capacity: " + capacity);
        capacityText.setLayoutX(220);
        capacityText.setLayoutY(160);

        Text descriptionText = new Text("Description: " + description);
        descriptionText.setLayoutX(220);
        descriptionText.setLayoutY(186);
        descriptionText.setWrappingWidth(600);

        Button bookNowButton = new Button("Book Now");
        bookNowButton.setLayoutX(218);
        bookNowButton.setLayoutY(199);
        bookNowButton.setStyle("-fx-background-color: #4CAF50;");


        ImageView paintingImageView = new ImageView();
        paintingImageView.setFitWidth(150);
        paintingImageView.setFitHeight(150);
        paintingImageView.setLayoutX(20);
        paintingImageView.setLayoutY(20);

        File imageFile = new File(imageUrl);
        if (imageFile.exists()) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                paintingImageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
                setPlaceholderImage(paintingImageView);
            }
        } else {
            System.out.println("Image file not found: " + imageUrl);
            setPlaceholderImage(paintingImageView);
        }

        card.getChildren().addAll(eventName, categoryText, dateTimeText, venueText, entryFeeText, capacityText, descriptionText, bookNowButton, paintingImageView);

        return card;
    }

    private void setPlaceholderImage(ImageView imageView) {
        File placeholderFile = new File(IMAGE_DIRECTORY, "placeholder.png");
        if (placeholderFile.exists()) {
            imageView.setImage(new Image(placeholderFile.toURI().toString()));
        } else {
            System.out.println("Placeholder image not found");
        }
    }
    // Include the navigation methods from the original guestPageController
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