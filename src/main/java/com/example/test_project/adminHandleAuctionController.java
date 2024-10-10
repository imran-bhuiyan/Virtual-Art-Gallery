package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class adminHandleAuctionController extends BaseController {

    @FXML
    private VBox auctionRequestsContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();
    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Uploads";

    @FXML
    public void initialize() {
        loadAuctionRequests();
    }

    private void loadAuctionRequests() {
        auctionRequestsContainer.getChildren().clear();
        String query = "SELECT a.auction_id, a.starting_bid, a.ends_time, p.name, p.image_url, p.price " +
                "FROM auctions a JOIN paintings p ON a.painting_id = p.painting_id " +
                "WHERE a.status = 'pending'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                auctionRequestsContainer.getChildren().add(createAuctionRequestCard(
                        rs.getInt("auction_id"),
                        rs.getDouble("starting_bid"),
                        rs.getTimestamp("ends_time").toLocalDateTime(),
                        rs.getString("name"),
                        rs.getString("image_url"),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private javafx.scene.layout.HBox createAuctionRequestCard(int auctionId, double startingBid, LocalDateTime endsTime, String paintingName, String imageUrl, double price) {
        javafx.scene.layout.HBox card = new javafx.scene.layout.HBox(10);
        card.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        ImageView artworkImageView = new ImageView();
        artworkImageView.setFitWidth(150);
        artworkImageView.setFitHeight(150);

        File imageFile = new File(imageUrl);
        if (imageFile.exists()) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                artworkImageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
                setPlaceholderImage(artworkImageView);
            }
        } else {
            System.out.println("Image file not found: " + imageUrl);
            setPlaceholderImage(artworkImageView);
        }

        VBox details = new VBox(5);
        details.getChildren().addAll(
                new Text("Painting: " + paintingName),
                new Text("Starting Bid: $" + startingBid),
                new Text("Original Price: $" + price),
                new Text("Ends at: " + endsTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        );

        Button acceptButton = new Button("Accept");
        acceptButton.setOnAction(e -> handleAuctionRequest(auctionId, true));
        styleButton(acceptButton, "-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button declineButton = new Button("Decline");
        declineButton.setOnAction(e -> handleAuctionRequest(auctionId, false));
        styleButton(declineButton, "-fx-background-color: #f44336; -fx-text-fill: white;");

        HBox buttonBox = new HBox(5, acceptButton, declineButton);
        buttonBox.setAlignment(Pos.TOP_RIGHT);

        VBox contentBox = new VBox(10, buttonBox, details);
        contentBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        card.getChildren().addAll(artworkImageView, contentBox);
        return card;
    }

    private void styleButton(Button button, String style) {
        button.setStyle(style + "-fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setStyle(style + "-fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand; -fx-opacity: 0.8;"));
        button.setOnMouseExited(e -> button.setStyle(style + "-fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;"));
    }

    private void setPlaceholderImage(ImageView imageView) {
        File placeholderFile = new File(IMAGE_DIRECTORY, "placeholder.png");
        if (placeholderFile.exists()) {
            imageView.setImage(new Image(placeholderFile.toURI().toString()));
        } else {
            System.out.println("Placeholder image not found");
        }
    }

    private void handleAuctionRequest(int auctionId, boolean isAccepted) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // Get user_id and painting name for the notification
            String userPaintingQuery = "SELECT p.artist_id, p.name FROM auctions a " +
                    "JOIN paintings p ON a.painting_id = p.painting_id " +
                    "WHERE a.auction_id = ?";
            try (PreparedStatement userPaintingStmt = conn.prepareStatement(userPaintingQuery)) {
                userPaintingStmt.setInt(1, auctionId);
                ResultSet rs = userPaintingStmt.executeQuery();
                if (rs.next()) {
                    int ar_id = rs.getInt("artist_id");
                    String paintingName = rs.getString("name");

                    // Insert notification first
                    String notificationQuery = "INSERT INTO notifications (user_id, type, title, message, created_at) " +
                            "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement notificationStmt = conn.prepareStatement(notificationQuery)) {
                        notificationStmt.setInt(1, ar_id);
                        notificationStmt.setString(2, "auction_request");
                        notificationStmt.setString(3, isAccepted ? "Auction Request Accepted" : "Auction Request Declined");
                        String message = isAccepted
                                ? "Your painting '" + paintingName + "' has been accepted for auction."
                                : "Your painting '" + paintingName + "' for auction was declined by the admin.";
                        notificationStmt.setString(4, message);
                        notificationStmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                        notificationStmt.executeUpdate();
                    }

                    // After inserting the notification, proceed with accept or decline
                    String auctionQuery;
                    if (isAccepted) {
                        auctionQuery = "UPDATE auctions SET status = 'active' WHERE auction_id = ?";
                    } else {
                        auctionQuery = "DELETE FROM auctions WHERE auction_id = ?";
                    }

                    try (PreparedStatement pstmt = conn.prepareStatement(auctionQuery)) {
                        pstmt.setInt(1, auctionId);
                        int affectedRows = pstmt.executeUpdate();

                        if (affectedRows > 0) {
                            conn.commit();  // Commit transaction
                            System.out.println("Auction request " + (isAccepted ? "accepted" : "declined") + " successfully");
                            loadAuctionRequests(); // Refresh the list
                        }
                    }
                }
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();  // Rollback transaction on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);  // Reset auto-commit to true
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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

