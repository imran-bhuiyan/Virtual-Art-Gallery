package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistNFTPageController extends BaseController {

    @FXML
    private VBox nftsContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();
    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Uploads";

    @FXML
    public void initialize() {
        loadNFTs();
    }

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("ArtistNFTPageController: setUserId() called with userId: " + userId);
        loadNFTs();
    }

    private void loadNFTs() {
        nftsContainer.getChildren().clear();

        String query = "SELECT name, edition, blockchain, price, quantity, payment_address, image_url, owner_id " +
                "FROM nfts " +
                "WHERE artist_id = ? AND owner_id IS NULL";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AnchorPane nftCard = createNFTCard(
                            rs.getString("name"),
                            rs.getString("edition"),
                            rs.getString("blockchain"),
                            rs.getBigDecimal("price"),
                            rs.getInt("quantity"),
                            rs.getString("payment_address"),
                            rs.getString("image_url")
                    );
                    nftsContainer.getChildren().add(nftCard);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load NFTs: " + e.getMessage());
        }
    }

    private AnchorPane createNFTCard(String name, String edition, String blockchain, BigDecimal price, int quantity, String paymentAddress, String imageUrl) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(860, 240);

        card.setStyle("-fx-background-color: #F0F8FF; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Text nameText = new Text(name);
        nameText.setLayoutX(220);
        nameText.setLayoutY(30);
        nameText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Text editionText = new Text("Edition: " + edition);
        editionText.setLayoutX(220);
        editionText.setLayoutY(57);

        Text blockchainText = new Text("Blockchain: " + blockchain);
        blockchainText.setLayoutX(220);
        blockchainText.setLayoutY(84);

        Text priceText = new Text("Price: $" + price.toPlainString());
        priceText.setLayoutX(220);
        priceText.setLayoutY(111);

        Text quantityText = new Text("Quantity: " + quantity);
        quantityText.setLayoutX(220);
        quantityText.setLayoutY(138);

        Text paymentAddressText = new Text("Payment Address: " + paymentAddress);
        paymentAddressText.setLayoutX(220);
        paymentAddressText.setLayoutY(165);

        ImageView nftImageView = new ImageView();
        nftImageView.setFitWidth(150);
        nftImageView.setFitHeight(150);
        nftImageView.setLayoutX(20);
        nftImageView.setLayoutY(20);

        File imageFile = new File(imageUrl);
        if (imageFile.exists()) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                nftImageView.setImage(image);
                nftImageView.setOnMouseClicked(event -> openImageInNewWindow(image));
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
                setPlaceholderImage(nftImageView);
            }
        } else {
            System.out.println("Image file not found: " + imageUrl);
            setPlaceholderImage(nftImageView);
        }

        card.getChildren().addAll(nameText, editionText, blockchainText, priceText, quantityText, paymentAddressText, nftImageView);

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
        Stage newWindow = new Stage();
        newWindow.setTitle("Zoomed Image");

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(500);
        imageView.setFitHeight(600);

        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, 500, 600);

        newWindow.setScene(scene);
        newWindow.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Navigation methods (same as in ArtistPaintingPageController)
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
        System.out.println("ArtistNFTPageController: Logging out user");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/CustomerLoginPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        System.out.println("ArtistNFTPageController: Navigated to login page");
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
        System.out.println("ArtistNFTPageController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("ArtistNFTPageController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("ArtistNFTPageController: Warning - controller is null");
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