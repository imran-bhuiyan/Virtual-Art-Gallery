package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerEventController extends BaseController {

    @FXML
    private VBox eventContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();
    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Uploads";



    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("CustomerEventController: setUserId() called with userId: " + userId);
        loadEventsFromDatabase();
    }

    private void loadEventsFromDatabase() {
        String query = "SELECT * FROM events where date_time > NOW()";
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
            showAlert("Database Error", "Failed to load events from database.");
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
        bookNowButton.setOnAction(event -> handleBooking(eventId, capacity,entryFee));

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

    private void handleBooking(int eventId, int currentCapacity, String entryFee) {
        try (Connection conn = dbConnection.getConnection()) {
            // Check the user's current credits
            String getCreditsQuery = "SELECT credits FROM users WHERE user_id = ?";
            try (PreparedStatement creditsStmt = conn.prepareStatement(getCreditsQuery)) {
                creditsStmt.setInt(1, userId);
                ResultSet creditsRs = creditsStmt.executeQuery();
                if (creditsRs.next()) {
                    double userCredits = creditsRs.getDouble("credits");
                    double fee = Double.parseDouble(entryFee);

                    // Check if the user has enough credits to book the event
                    if (userCredits < fee) {
                        showSweetAlert("Insufficient Balance", "You do not have enough credits to book this event.");
                        return;
                    }
                }
            }

            // Check if the user has already booked this event
            String checkBookingQuery = "SELECT * FROM eventbookings WHERE user_id = ? AND event_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkBookingQuery)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, eventId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    showSweetAlert("Already Booked", "You have already booked this event.");
                    return;
                }
            }

            // Check if there's still capacity
            if (currentCapacity <= 0) {
                showSweetAlert("Booking Failed", "Sorry, this event is fully booked.");
                return;
            }

            // Proceed with booking and deduct credits
            String bookingQuery = "INSERT INTO eventbookings (user_id, event_id) VALUES (?, ?)";
            String updateCapacityQuery = "UPDATE events SET capacity = capacity - 1 WHERE event_id = ?";
            String updateCreditsQuery = "UPDATE users SET credits = credits - ? WHERE user_id = ?";
            String updateAdminCreditsQuery = "UPDATE users SET credits = credits + ? WHERE role = 'admin'";

            conn.setAutoCommit(false);
            try {
                // Insert booking
                try (PreparedStatement bookingStmt = conn.prepareStatement(bookingQuery)) {
                    bookingStmt.setInt(1, userId);
                    bookingStmt.setInt(2, eventId);
                    bookingStmt.executeUpdate();
                }

                // Update event capacity
                try (PreparedStatement updateStmt = conn.prepareStatement(updateCapacityQuery)) {
                    updateStmt.setInt(1, eventId);
                    updateStmt.executeUpdate();
                }

                // Deduct the entry fee from the user's credits
                try (PreparedStatement updateAdq = conn.prepareStatement(updateAdminCreditsQuery)) {
                    updateAdq.setDouble(1, Double.parseDouble(entryFee));
                    updateAdq.executeUpdate();
                }


                // Deduct the entry fee from the user's credits
                try (PreparedStatement updateCreditsStmt = conn.prepareStatement(updateCreditsQuery)) {
                    updateCreditsStmt.setDouble(1, Double.parseDouble(entryFee));
                    updateCreditsStmt.setInt(2, userId);
                    updateCreditsStmt.executeUpdate();
                }

                conn.commit();
                showSweetAlert("Booking Successful", "You have successfully booked this event.");
//                loadEventsFromDatabase(); // Refresh the event display
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                showSweetAlert("Booking Failed", "An error occurred while booking the event.");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showSweetAlert("Database Error", "An error occurred while connecting to the database.");
        }
    }

    private void showSweetAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    private void setPlaceholderImage(ImageView imageView) {
        File placeholderFile = new File(IMAGE_DIRECTORY, "placeholder.png");
        if (placeholderFile.exists()) {
            imageView.setImage(new Image(placeholderFile.toURI().toString()));
        } else {
            System.out.println("Placeholder image not found");
        }
    }




    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
        System.out.println("CustomerHomePage : Logging out user");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/customerLoginPage.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

        System.out.println("customerPaintingController: Navigated to login page");
    }

    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("customerPaintingController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("customerPaintingController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("customerPaintingController: Warning - controller is null");
        }

        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}