package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistPaintingPageController extends BaseController {

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
    }

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("ArtistPaintingPageController: setUserId() called with userId: " + userId);
        loadPaintings();
    }

    private void loadPaintings() {
        paintingsContainer.getChildren().clear();
        String selectedCategory = categoryComboBox.getValue();

        String query = "SELECT p.painting_id, p.name as Title, u.name as Artist, p.year, p.category, p.price, p.image_url,  COUNT(r.painting_id) as Reaction " +
                "FROM paintings as p JOIN users as u ON p.artist_id = u.user_id " +
                "LEFT JOIN reactions as r ON r.painting_id = p.painting_id " +
                "WHERE p.category = ? OR ? = 'All Categories' and p.painting_status ='active' " +
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
                            rs.getInt("painting_id")
                    );
                    paintingsContainer.getChildren().add(paintingCard);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    private AnchorPane createPaintingCard(String title, String artist, int year, String category, double price, int reactions, String imageUrl, int paintingId) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(860, 213);

        card.setStyle("-fx-background-color: #F0F8FF; -fx-border-color: #cccccc; -fx-border-width: 1;");

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

        Button reactionButton = new Button();
        reactionButton.setLayoutX(218);
        reactionButton.setLayoutY(153);



        Image loveImage = new Image("file:/D:/Trimester/8th/AOOP/IntellijIdea/Test_Project/src/main/java/Icon/Love.png");
        ImageView imageView = new ImageView(loveImage);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        reactionButton.setGraphic(imageView);

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
                paintingImageView.setOnMouseClicked(event -> openImageInNewWindow(image));
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
                setPlaceholderImage(paintingImageView);
            }
        } else {
            System.out.println("Image file not found: " + imageUrl);
            setPlaceholderImage(paintingImageView);
        }

        card.getChildren().addAll(titleText, artistText, yearText, categoryText, priceText, reactionText,  reactionButton, paintingImageView);

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


    private void openImageInNewWindow(Image image) {
        // Create a new stage (window)
        Stage newWindow = new Stage();
        newWindow.setTitle("Zoomed Image");

        // Create an ImageView for displaying the image
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true); // Keep aspect ratio

        // Set the fixed size for the ImageView (500x600)
        imageView.setFitWidth(500);
        imageView.setFitHeight(600);

        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, 500, 600);

        // Set the scene and show the new window
        newWindow.setScene(scene);
        newWindow.show();
    }


    // Navigation methods
    @FXML
    void artistAddAuctions(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddAuction.fxml");
    }

    @FXML
    void artistAddNFTs(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddNFTs.fxml");
    }

    @FXML
    void artistDashboard(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistDashboard.fxml");
    }

    @FXML
    void artistLogout(ActionEvent event) throws IOException {
        System.out.println("ArtistAddNFTController: Logging out user");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/CustomerLoginPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        System.out.println("ArtistAddNFTController: Navigated to login page");
    }

    @FXML
    void artistMessages(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistMessage.fxml");
    }

    @FXML
    void artistMyPainting(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistMyPaintPage.fxml");
    }

    @FXML
    void artistMyProfile(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistProfile.fxml");
    }

    @FXML
    void artistNotification(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNotification.fxml");
    }

    @FXML
    void artistPaintings(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistPaintingPage.fxml");
    }

    @FXML
    void artistSeeAuction(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistSeeAuctionPage.fxml");
    }

    @FXML
    void artistOrders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistOrderPage.fxml");
    }


    @FXML
    void artistAddPainting(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddPaint.fxml");
    }

    @FXML
    void mynft(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNFTPage.fxml");
    }


    @FXML
    void nftorders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNFTorders.fxml");

    }

    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("ArtistAddNFTController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("ArtistAddNFTController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("ArtistAddNFTController: Warning - controller is null");
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