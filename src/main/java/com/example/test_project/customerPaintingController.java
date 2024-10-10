package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class customerPaintingController extends BaseController {

    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private VBox paintingsContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();
    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Uploads";



    @FXML
    public void initialize() {
        categoryComboBox.getItems().addAll("All Categories", "Oil paint", "Acrylic paint", "Watercolor", "Gouache", "Tempera", "Encaustic", "Casein", "Alkyd");
        categoryComboBox.setValue("All Categories");
        categoryComboBox.setOnAction(event -> loadPaintings());
//        loadPaintings();
    }

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("CustomerPaintingController: setUserId() called with userId: " + userId);
        loadPaintings();
    }



    private void loadPaintings() {
        paintingsContainer.getChildren().clear();
        String selectedCategory = categoryComboBox.getValue();

        String query = "SELECT p.artist_id,p.painting_id, p.name as Title, u.name as Artist, p.year, p.category, p.price, p.image_url,COUNT(r.painting_id) as Reaction " +
                "FROM paintings as p JOIN users as u ON p.artist_id = u.user_id " +
                "LEFT JOIN reactions as r ON r.painting_id = p.painting_id " +
                "WHERE p.category = ? OR ? = 'All Categories' " +
                "GROUP BY p.painting_id";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, selectedCategory);
            pstmt.setString(2, selectedCategory);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AnchorPane paintingCard = createPaintingCard(

                            rs.getString("Title"),
                            rs.getString("Artist"),
                            rs.getInt("year"),
                            rs.getString("category"),
                            rs.getDouble("price"),
                            rs.getInt("Reaction"),
                            rs.getString("image_url"),
                            rs.getInt("painting_id"),
                            rs.getInt("artist_id")

                            );
                    paintingsContainer.getChildren().add(paintingCard);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    private AnchorPane createPaintingCard(String title, String artist, int year, String category, double price, int reactions , String imageUrl,int paintingId,int artistId) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(860, 213);
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

        Text reactionText = new Text(String.valueOf(reactions));
        reactionText.setLayoutX(269);
        reactionText.setLayoutY(170);
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setLayoutX(800);
        addToCartButton.setLayoutY(10);
        addToCartButton.setStyle("-fx-background-color: #40E0D0;");
        addToCartButton.setOnAction(event -> addToCart(paintingId, title));

        Button chatWithArtistButton = new Button("Chat with Artist");
        chatWithArtistButton.setLayoutX(218);
        chatWithArtistButton.setLayoutY(190);
        chatWithArtistButton.setStyle("-fx-background-color: #4CAF50;");
        chatWithArtistButton.setOnAction(event -> openChatWithArtist(event, artistId, artist));

        Button reactionButton = new Button();
        reactionButton.setLayoutX(218);
        reactionButton.setLayoutY(153);

        // Create Image and ImageView
        Image loveImage = new Image("file:/D:/Trimester/8th/AOOP/IntellijIdea/Test_Project/src/main/java/Icon/Love.png");
        ImageView imageView = new ImageView(loveImage);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        reactionButton.setGraphic(imageView);


        boolean hasReacted = checkUserReaction(paintingId);
        updateReactionButtonStyle(reactionButton, hasReacted);

        reactionButton.setOnAction(event -> handleReaction(paintingId, reactionButton, reactionText));


        ImageView paintingImageView = new ImageView();
        paintingImageView.setFitWidth(150);
        paintingImageView.setFitHeight(150);
        paintingImageView.setLayoutX(20);
        paintingImageView.setLayoutY(20);


        //Loading the image
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

        card.getChildren().addAll(titleText, artistText, yearText, categoryText, priceText, reactionText, addToCartButton, chatWithArtistButton, reactionButton,paintingImageView);

        return card;
    }

    //setting image

    private void setPlaceholderImage(ImageView imageView) {
        // Set a placeholder image
        File placeholderFile = new File(IMAGE_DIRECTORY, "placeholder.png");
        if (placeholderFile.exists()) {
            imageView.setImage(new Image(placeholderFile.toURI().toString()));
        } else {
            System.out.println("Placeholder image not found");
        }
    }

    // load chat
    private void openChatWithArtist(ActionEvent event, int artistId, String artistName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/customerMessengerPage.fxml"));
            Parent root = loader.load();

            CustomerMessageController controller = loader.getController();
            if (controller != null) {
                controller.setUserId(this.userId);
                controller.initChat(artistId, artistName);
            } else {
                System.out.println("customerPaintingController: Warning - controller is null");
            }

            Stage stage = new Stage();
            stage.setTitle("Chat with " + artistName);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error opening chat window: " + e.getMessage());
        }
    }


    // reaction check
    private boolean checkUserReaction(int paintingId) {
        String query = "SELECT * FROM reactions WHERE user_id = ? AND painting_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, paintingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // If there's a result, the user has reacted
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // reaction button style
    private void updateReactionButtonStyle(Button button, boolean hasReacted) {
        if (hasReacted) {
            button.setStyle("-fx-background-color: Red;"); // Pink background for reacted
        } else {
            button.setStyle("-fx-background-color: #FFFFFF;"); // White background for not reacted
        }
    }

    // handle reaction in database
    private void handleReaction(int paintingId, Button reactionButton, Text reactionText) {
        boolean currentlyReacted = checkUserReaction(paintingId);
        String query;
        if (currentlyReacted) {
            query = "DELETE FROM reactions WHERE user_id = ? AND painting_id = ?";
        } else {
            query = "INSERT INTO reactions (user_id, painting_id) VALUES (?, ?)";
        }

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, paintingId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Update UI
                updateReactionButtonStyle(reactionButton, !currentlyReacted);
                int newReactionCount = Integer.parseInt(reactionText.getText()) + (currentlyReacted ? -1 : 1);
                reactionText.setText(String.valueOf(newReactionCount));
            }

            // Handle notification
            if (currentlyReacted) {
                removeNotification(paintingId);
            } else {
                addNotification(paintingId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }


    private void addNotification(int paintingId) {
        String query = "INSERT INTO notifications (user_id, type, title, message, created_at) " +
                "SELECT artist_id, 'reaction', 'New Reaction', CONCAT(?, ' reacted to your painting'), NOW() " +
                "FROM paintings WHERE painting_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, getUserName()); // Assuming you have a method to get the current user's name
            pstmt.setInt(2, paintingId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }

    private void removeNotification(int paintingId) {
        String query = "DELETE FROM notifications " +
                "WHERE user_id = (SELECT artist_id FROM paintings WHERE painting_id = ?) " +
                "AND type = 'reaction' " +
                "AND message LIKE CONCAT(?, ' reacted to your painting')";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, paintingId);
            pstmt.setString(2, getUserName()); // Assuming you have a method to get the current user's name
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }

    private String getUserName() {
        String query = "SELECT name FROM users WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown User";
    }


    // Cart handle
    private void addToCart(int paintingId, String paintingTitle) {
        String query = "INSERT INTO cart (user_id, painting_id, quantity) VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE quantity = quantity + 1";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, paintingId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Item added to cart successfully");

                showSweetAlert(paintingTitle);
            } else {
                System.out.println("Failed to add item to cart");
                showErrorAlert();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error adding item to cart: " + e.getMessage());
            showErrorAlert();
        }
    }

    private void showSweetAlert(String paintingTitle) {
        Notifications.create()
                .title("Added to Cart")
                .text(paintingTitle + " has been added to your cart.")

                .position(Pos.CENTER)
                .hideAfter(Duration.seconds(5))
                .show();
    }

    private void showErrorAlert() {
        Notifications.create()
                .title("Error")
                .text("Failed to add item to cart. Please try again.")

                .position(Pos.CENTER)
                .hideAfter(Duration.seconds(5))
                .showError();
    }




    @FXML
    void goCustomerAuction(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerLiveAuctionPage.fxml");

    }

    @FXML
    void goCustomerCart(ActionEvent event) throws IOException {
        loadPageWithUserId(event,"Customer/customerPaintingCheckout.fxml");

    }

    @FXML
    void goCustomerEvents(ActionEvent event) throws IOException {
        loadPageWithUserId(event,"Customer/customerEventPage.fxml");

    }


    @FXML
    void goCustomerHome(ActionEvent event) throws IOException {
        loadPageWithUserId(event,"Customer/customerHomePage.fxml");

    }

    @FXML
    void goCustomerMessages(ActionEvent event) throws IOException {
        System.out.println("CustomerHomePageController: Navigating to Messages page");

        // Instead of direct FXMLLoader usage, use BaseController's loadPage method
        loadPageWithUserId(event, "Customer/customerMessengerPage.fxml");

    /* No need for manual controller setup and userId setting because:
       1. BaseController.loadPage() automatically handles loading the FXML
       2. It automatically passes the userId to the new controller via setUserId()
       3. The CustomerMessengerController extends BaseController and implements proper setUserId()
    */
    }

    @FXML
    void goCustomerNFT(ActionEvent event) throws IOException {
        loadPageWithUserId(event,"Customer/customerNFTpage.fxml");

    }

    @FXML
    void goCustomerNotification(ActionEvent event) throws IOException {
        loadPageWithUserId(event,"Customer/customerNotification.fxml");

    }

    @FXML
    void goCustomerPaintings(ActionEvent event) throws IOException {
        loadPageWithUserId(event,"Customer/customerPaintingPage.fxml");

    }

    @FXML
    void goCustomerProfile(ActionEvent event) throws IOException {
        loadPageWithUserId(event,"Customer/customerProfilePage.fxml");

    }

    @FXML
    void goCustomerTopArt(ActionEvent event) throws IOException {
        loadPageWithUserId(event,"Customer/customerPageTopArt.fxml");

    }

    @FXML
    void customerLogout(ActionEvent event) throws IOException {
        System.out.println("CustomerHomePage : Logging out user");
        // Clear any user-specific data or session information here if needed

        // Navigate to the login page
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