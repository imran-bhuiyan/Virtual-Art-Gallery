package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class adminManageArtController extends BaseController {

    @FXML
    private TextField searchField;

    @FXML
    private VBox artworksContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();
    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Uploads";

    @FXML
    public void initialize() {
        loadArtworks();
    }

    @FXML
    void searchArtwork(ActionEvent event) {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            loadArtworks("SELECT p.painting_id, p.name as Title, u.name as Artist, p.year, p.category, p.price, p.image_url " +
                    "FROM paintings as p JOIN users as u ON p.artist_id = u.user_id " +
                    "WHERE p.name LIKE ?", "%" + searchTerm + "%");
        }
    }

    @FXML
    void seeAllArtworks(ActionEvent event) {
        loadArtworks();
    }

    private void loadArtworks() {
        loadArtworks("SELECT p.painting_id, p.name as Title, u.name as Artist, p.year, p.category, p.price, p.image_url " +
                "FROM paintings as p JOIN users as u ON p.artist_id = u.user_id", null);
    }

    private void loadArtworks(String query, String searchTerm) {
        artworksContainer.getChildren().clear();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (searchTerm != null) {
                pstmt.setString(1, searchTerm);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AnchorPane card = createArtworkCard(
                            rs.getInt("painting_id"),
                            rs.getString("Title"),
                            rs.getString("Artist"),
                            rs.getInt("year"),
                            rs.getString("category"),
                            rs.getDouble("price"),
                            rs.getString("image_url")
                    );
                    artworksContainer.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private AnchorPane createArtworkCard(int paintingId, String title, String artist, int year, String category, double price, String imageUrl) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(660, 213);
        card.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Text titleText = new Text(title);
        titleText.setLayoutX(220);
        titleText.setLayoutY(30);
        titleText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Text artistText = new Text("Artist: " + artist);
        artistText.setLayoutX(220);
        artistText.setLayoutY(57);

        Text yearText = new Text("Year: " + year);
        yearText.setLayoutX(220);
        yearText.setLayoutY(87);

        Text categoryText = new Text("Category: " + category);
        categoryText.setLayoutX(220);
        categoryText.setLayoutY(114);

        Text priceText = new Text("Price: $" + price);
        priceText.setLayoutX(220);
        priceText.setLayoutY(142);

        Button editButton = new Button("Edit");
        editButton.setLayoutX(600);
        editButton.setLayoutY(10);
        editButton.setStyle("-fx-background-color:#C1E2A4;");
        editButton.setOnAction(event -> openEditArtworkPage(paintingId));

        // Add Discontinue button
        Button discontinueButton = new Button("Discontinue");
        discontinueButton.setLayoutX(600);
        discontinueButton.setLayoutY(50);
        discontinueButton.setStyle("-fx-background-color:RED;");
        discontinueButton.setOnAction(event -> discontinuePainting(paintingId, title));

        ImageView artworkImageView = new ImageView();
        artworkImageView.setFitWidth(150);
        artworkImageView.setFitHeight(150);
        artworkImageView.setLayoutX(20);
        artworkImageView.setLayoutY(20);

        // Loading the image
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

        card.getChildren().addAll(titleText, artistText, yearText, categoryText, priceText, editButton, artworkImageView, discontinueButton);

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

    private void openEditArtworkPage(int paintingId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Admin/AdminEditArtwork.fxml"));
            Parent root = loader.load();

            // Assume you have an AdminEditArtworkController
            adminEditArtworkController controller = loader.getController();
            controller.setPaintingId(paintingId);
            controller.setUserId(this.userId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }



    private void discontinuePainting(int paintingId, String paintingName) {
        // Step 1: Confirmation Alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to discontinue this painting?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            try (Connection conn = dbConnection.getConnection()) {
                // Step 2: Check if the painting is already discontinued
                String checkStatusQuery = "SELECT painting_status FROM paintings WHERE painting_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkStatusQuery);
                checkStmt.setInt(1, paintingId);
                ResultSet statusRs = checkStmt.executeQuery();

                if (statusRs.next() && "deactive".equals(statusRs.getString("painting_status"))) {
                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION, "This painting is already discontinued.");
                    infoAlert.show();
                    return;
                }

                // Step 3: Update painting status to deactive
                String updateStatusQuery = "UPDATE paintings SET painting_status = 'deactive' WHERE painting_id = ?";
                PreparedStatement updateStatusStmt = conn.prepareStatement(updateStatusQuery);
                updateStatusStmt.setInt(1, paintingId);
                updateStatusStmt.executeUpdate();

                // Step 4: Get artist_id for the painting
                String getArtistQuery = "SELECT artist_id FROM paintings WHERE painting_id = ?";
                PreparedStatement getArtistStmt = conn.prepareStatement(getArtistQuery);
                getArtistStmt.setInt(1, paintingId);
                ResultSet artistRs = getArtistStmt.executeQuery();

                if (artistRs.next()) {
                    int artistId = artistRs.getInt("artist_id");

                    // Step 5: Create notification for the artist
                    String notificationMessage = "Your painting '" + paintingName + "' has been discontinued.";
                    String insertNotificationQuery = "INSERT INTO notifications (user_id, title, message, created_at) VALUES (?, ?, ?, NOW())";
                    PreparedStatement insertNotificationStmt = conn.prepareStatement(insertNotificationQuery);
                    insertNotificationStmt.setInt(1, artistId);
                    insertNotificationStmt.setString(2, "Painting Discontinued");
                    insertNotificationStmt.setString(3, notificationMessage);
                    insertNotificationStmt.executeUpdate();
                }

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Painting discontinued successfully!");
                successAlert.show();

            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to discontinue the painting.");
                errorAlert.show();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            controller.setUserId(this.userId);
        }

        Scene scene = new Scene(root);
        Stage stage;

        if (event.getSource() instanceof MenuItem) {
            MenuItem menuItem = (MenuItem) event.getSource();
            stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
        } else if (event.getSource() instanceof Node) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Event source not recognized");
        }

        stage.setScene(scene);
        stage.show();
    }
}
