package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminManageEventController extends BaseController {

    @FXML
    private TextField searchField;

    @FXML
    private VBox eventsContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();

    @FXML
    public void initialize() {
        loadEvents();
    }

    @FXML
    void searchEvent(ActionEvent event) {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            loadEvents("SELECT * FROM events WHERE name LIKE ?", "%" + searchTerm + "%");
        }
    }

    @FXML
    void seeAllEvents(ActionEvent event) {
        loadEvents();
    }

    private void loadEvents() {
        loadEvents("SELECT * FROM events", null);
    }

    private void loadEvents(String query, String searchTerm) {
        eventsContainer.getChildren().clear();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (searchTerm != null) {
                pstmt.setString(1, searchTerm);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AnchorPane card = createEventCard(
                            rs.getInt("event_id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getDate("date_time"),
                            rs.getString("time"),
                            rs.getString("venue"),
                            rs.getDouble("entry_fee"),
                            rs.getString("picture_url"),
                            rs.getString("description"),
                            rs.getInt("capacity")
                    );
                    eventsContainer.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private AnchorPane createEventCard(int eventId, String name, String category, java.sql.Date date, String time,
                                       String venue, double entryFee, String imageUrl, String description, int capacity) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(660, 213);
        card.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");

        ImageView eventImageView = new ImageView();
        eventImageView.setFitWidth(150);
        eventImageView.setFitHeight(150);
        eventImageView.setLayoutX(20);
        eventImageView.setLayoutY(20);

        // Loading the image
        File imageFile = new File(imageUrl);
        if (imageFile.exists()) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                eventImageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
                // Set a placeholder image
            }
        } else {
            System.out.println("Image file not found: " + imageUrl);
            // Set a placeholder image
        }

        Text nameText = new Text(name);
        nameText.setLayoutX(190);
        nameText.setLayoutY(30);
        nameText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Text categoryText = new Text("Category: " + category);
        categoryText.setLayoutX(190);
        categoryText.setLayoutY(60);

        Text dateTimeText = new Text("Date & Time: " + date + " " + time);
        dateTimeText.setLayoutX(190);
        dateTimeText.setLayoutY(90);

        Text venueText = new Text("Venue: " + venue);
        venueText.setLayoutX(190);
        venueText.setLayoutY(120);

        Text feeText = new Text("Entry Fee: $" + entryFee);
        feeText.setLayoutX(190);
        feeText.setLayoutY(150);

        Text capacityText = new Text("Capacity: " + capacity);
        capacityText.setLayoutX(190);
        capacityText.setLayoutY(180);

        Button editButton = new Button("Edit");
        editButton.setLayoutX(600);
        editButton.setLayoutY(10);

// Set a wider button and improve appearance with padding and font size
        editButton.setStyle(
                "-fx-background-color: #40E0D0;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 3 16;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );

// Set minimum width for the button
        editButton.setMinWidth(70);
        editButton.setOnAction(event -> openEditEventPage(eventId));


        // Delete Button .
        Button deleteButton = new Button("Delete");
        deleteButton.setLayoutX(600);
        deleteButton.setLayoutY(50);

        deleteButton.setStyle(
                "-fx-background-color: red;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 3 16;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );

// Set minimum width for the button
        deleteButton.setMinWidth(70);
        deleteButton.setOnAction(event -> deleteEvent(eventId, entryFee,name));


        card.getChildren().addAll(eventImageView, nameText, categoryText, dateTimeText, venueText, feeText, capacityText, editButton,deleteButton);

        return card;
    }

    // delete events

    private void deleteEvent(int eventId, double entryFee,String name) {
        // Step 1: Confirmation Alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this event?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            try (Connection conn = dbConnection.getConnection()) {

                // Step 2: Check if the event is ended
                String checkStatusQuery = "SELECT event_status FROM events WHERE event_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkStatusQuery);
                checkStmt.setInt(1, eventId);
                ResultSet statusRs = checkStmt.executeQuery();

                if (statusRs.next() && "ended".equals(statusRs.getString("event_status"))) {
                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION, "You cannot delete an ended event.");
                    infoAlert.show();
                    return;
                }

                // Step 3: Refund process
                String bookingQuery = "SELECT user_id FROM eventbookings WHERE event_id = ?";
                PreparedStatement bookingStmt = conn.prepareStatement(bookingQuery);
                bookingStmt.setInt(1, eventId);
                ResultSet bookingsRs = bookingStmt.executeQuery();

                double totalRefund = 0;
                while (bookingsRs.next()) {
                    int customerId = bookingsRs.getInt("user_id");

                    // Refund user's credits
                    String updateCreditsQuery = "UPDATE users SET credits = credits + ? WHERE user_id = ?";
                    PreparedStatement updateCreditsStmt = conn.prepareStatement(updateCreditsQuery);
                    updateCreditsStmt.setDouble(1, entryFee);
                    updateCreditsStmt.setInt(2, customerId);
                    updateCreditsStmt.executeUpdate();

                    totalRefund += entryFee;

                    // Step 4: Create notification for the user
                    String notificationMessage = "The event " + name + " , you registered for has been canceled, and a refund has been issued.";
                    String insertNotificationQuery = "INSERT INTO notifications (user_id, title, message, created_at) VALUES (?, ?, ?, NOW())";
                    PreparedStatement insertNotificationStmt = conn.prepareStatement(insertNotificationQuery);
                    insertNotificationStmt.setInt(1, customerId);
                    insertNotificationStmt.setString(2, "Event Cancellation");
                    insertNotificationStmt.setString(3, notificationMessage);
                    insertNotificationStmt.executeUpdate();
                }

                // Step 5: Deduct the total refund from the admin's account
                String deductAdminCreditsQuery = "UPDATE users SET credits = credits - ? WHERE role = 'admin'";
                PreparedStatement deductAdminStmt = conn.prepareStatement(deductAdminCreditsQuery);
                deductAdminStmt.setDouble(1, totalRefund);
                deductAdminStmt.executeUpdate();

                // Step 6: Delete event from eventbookings and events
                String deleteBookingsQuery = "DELETE FROM eventbookings WHERE event_id = ?";
                PreparedStatement deleteBookingsStmt = conn.prepareStatement(deleteBookingsQuery);
                deleteBookingsStmt.setInt(1, eventId);
                deleteBookingsStmt.executeUpdate();

                String deleteEventQuery = "DELETE FROM events WHERE event_id = ?";
                PreparedStatement deleteEventStmt = conn.prepareStatement(deleteEventQuery);
                deleteEventStmt.setInt(1, eventId);
                deleteEventStmt.executeUpdate();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Event deleted successfully!");
                successAlert.show();

            } catch (SQLException e) {
                e.printStackTrace();
                // Handle the exception
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to delete the event.");
                errorAlert.show();
            }
        }
    }

    private void openEditEventPage(int eventId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Admin/AdminEditEvent.fxml"));
            Parent root = loader.load();

            AdminEditEventController controller = loader.getController();
            controller.setEventId(eventId);
            controller.setUserId(this.userId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    // Navigation methods
    @FXML
    void goLogout(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerLoginPage.fxml");
    }

    @FXML
    void goDashboard(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminDashboard.fxml");
    }

    @FXML
    void addArtist(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddArtist.fxml");
    }

    @FXML
    void addArtworks(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddArtwork.fxml");
    }

    @FXML
    void addEvent(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddEvent.fxml");
    }

    @FXML
    void addCustomer(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAddCustomers.fxml");
    }

    @FXML
    void manageCustomer(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageCustomers.fxml");
    }

    @FXML
    void manageArtist(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageArtist.fxml");
    }

    @FXML
    void manageArtwork(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageArtwork.fxml");
    }

    @FXML
    void manageEvent(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminManageEvent.fxml");
    }

    @FXML
    void goOrders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminOrderPage.fxml");
    }

    @FXML
    void goMessages(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminMessages.fxml");
    }

    @FXML
    void goQueries(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminQueriesPage.fxml");
    }

    @FXML
    void goAuctionRequest(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAuctionPage.fxml");
    }


    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("AdminArtistAddController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("AdminArtistAddController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("AdminArtistAddController: Warning - controller is null");
        }

        Scene scene = new Scene(root);
        Stage stage;

        if (event.getSource() instanceof MenuItem) {
            // If the event source is a MenuItem, we need to get the stage differently
            MenuItem menuItem = (MenuItem) event.getSource();
            stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
        } else if (event.getSource() instanceof Node) {
            // If it's a regular Node (like a Button), we can get the stage as before
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Event source not recognized");
        }

        stage.setScene(scene);
        stage.show();
    }
}