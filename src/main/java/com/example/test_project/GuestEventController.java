package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class GuestEventController implements Initializable {

    @FXML
    private VBox eventContainer;

    private DataBaseConnection dbConnection;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbConnection = new DataBaseConnection();
        loadEventsFromDatabase();
    }

    private void loadEventsFromDatabase() {
        String query = "SELECT * FROM events";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                AnchorPane eventCard = createEventCard(
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("date_time"),
                        rs.getString("venue"),
                        rs.getString("entry_fee"),
                        rs.getString("description"),
                        rs.getInt("capacity")
                );
                eventContainer.getChildren().add(eventCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    private AnchorPane createEventCard(String name, String category, String dateTime, String venue, String entryFee, String description, int capacity) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(223);
        card.setPrefWidth(860);
        card.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Text eventName = new Text(name);
        eventName.setLayoutX(220);
        eventName.setLayoutY(30);
        eventName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Text categoryText = new Text("Category: " + category);
        categoryText.setLayoutX(220);
        categoryText.setLayoutY(60);

        Text dateTimeText = new Text("Date & Time: " + dateTime);
        dateTimeText.setLayoutX(220);
        dateTimeText.setLayoutY(90);

        Text venueText = new Text("Venue: " + venue);
        venueText.setLayoutX(220);
        venueText.setLayoutY(120);

        Text entryFeeText = new Text("Entry Fee: " + entryFee);
        entryFeeText.setLayoutX(220);
        entryFeeText.setLayoutY(150);

        Text capacityText = new Text("Capacity: " + capacity);
        capacityText.setLayoutX(220);
        capacityText.setLayoutY(180);

        Button bookNowButton = new Button("Book Now");
        bookNowButton.setLayoutX(220);
        bookNowButton.setLayoutY(190);
        bookNowButton.setStyle("-fx-background-color: #4CAF50;");

        card.getChildren().addAll(eventName, categoryText, dateTimeText, venueText, entryFeeText, capacityText,bookNowButton);

        return card;
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