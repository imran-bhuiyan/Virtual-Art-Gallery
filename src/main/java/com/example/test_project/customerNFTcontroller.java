package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class customerNFTcontroller extends BaseController {

    @FXML
    private VBox nftContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();

    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Uploads";


    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("CustomerEventController: setUserId() called with userId: " + userId);
        loadNFTsFromDatabase();
    }

    private void loadNFTsFromDatabase() {
        String query = "SELECT n.nft_id, n.name as Title, artist.name as Artist, n.edition, n.blockchain, n.price, n.image_url, " +
                "owner.user_id as owner_id, owner.name as Owner " +
                "FROM nfts as n " +
                "JOIN users as artist ON n.artist_id = artist.user_id " +
                "LEFT JOIN users as owner ON n.owner_id = owner.user_id";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                AnchorPane nftCard = createNFTCard(
                        rs.getString("Title"),
                        rs.getString("Artist"),
                        rs.getString("edition"),
                        rs.getString("blockchain"),
                        rs.getString("price"),
                        rs.getString("image_url"),
                        rs.getInt("nft_id"),
                        rs.getInt("owner_id"),
                        rs.getString("Owner")
                );
                nftContainer.getChildren().add(nftCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    private AnchorPane createNFTCard(String title, String artist, String edition, String blockchain, String price, String imageUrl, int nftId, int ownerId, String ownerName) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(280.0);
        card.setPrefWidth(860.0);
        card.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(230.0);
        imageView.setFitWidth(200.0);
        imageView.setLayoutX(10.0);
        imageView.setLayoutY(10.0);
        imageView.setPreserveRatio(true);

        // Load the image
        File imageFile = new File(imageUrl);
        if (imageFile.exists()) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                imageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
                setPlaceholderImage(imageView);
            }
        } else {
            System.out.println("Image file not found: " + imageUrl);
            setPlaceholderImage(imageView);
        }

        Text titleText = new Text(title);
        titleText.setLayoutX(220.0);
        titleText.setLayoutY(30.0);
        titleText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Text artistText = new Text("Artist: " + artist);
        artistText.setLayoutX(220.0);
        artistText.setLayoutY(60.0);

        Text editionText = new Text("Edition: " + edition);
        editionText.setLayoutX(220.0);
        editionText.setLayoutY(90.0);

        Text blockchainText = new Text("Blockchain: " + blockchain);
        blockchainText.setLayoutX(220.0);
        blockchainText.setLayoutY(120.0);

        Text priceText = new Text("Price: " + price);
        priceText.setLayoutX(220.0);
        priceText.setLayoutY(150.0);

        Text ownerText = new Text("Owner: " + (ownerName != null ? ownerName : "Not owned"));
        ownerText.setLayoutX(220.0);
        ownerText.setLayoutY(180.0);
        ownerText.setStyle("-fx-font-weight: bold; -fx-fill: #4A90E2;");

        Button buyButton = new Button("Buy Now");
        buyButton.setLayoutX(220.0);
        buyButton.setLayoutY(210.0);
        buyButton.setStyle("-fx-background-color: #4CAF50;");
        buyButton.setOnAction(event -> openCheckoutPage(title, artist, price, nftId));

        Text infoText = new Text("After purchase, ownership will be transferred automatically and you can download your NFT.");
        infoText.setLayoutX(220.0);
        infoText.setLayoutY(250.0);
        infoText.setStyle("-fx-font-size: 12px;");

        card.getChildren().addAll(imageView, titleText, artistText, editionText, blockchainText, priceText, ownerText, infoText);

        // Only add the buy button if the NFT is not owned
        if (ownerId == 0) {
            card.getChildren().add(buyButton);
        }

        return card;
    }



    private void setPlaceholderImage(ImageView imageView) {
        // Set a placeholder image
        File placeholderFile = new File(IMAGE_DIRECTORY, "placeholder.png");
        if (placeholderFile.exists()) {
            imageView.setImage(new Image(placeholderFile.toURI().toString()));
        } else {
            System.out.println("Placeholder image not found");
        }
    }

    private void openCheckoutPage(String title, String artist, String price, int nftId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/customerNFTcheckoutPage.fxml"));
            Parent root = loader.load();

            CustomerNFTCheckoutController checkoutController = loader.getController();
            BigDecimal priceDecimal = new BigDecimal(price.replace("$", "").trim());
            checkoutController.setNFTDetails(nftId, priceDecimal);
            checkoutController.setUserId(this.userId);

            Stage checkoutStage = new Stage();
            checkoutStage.initModality(Modality.APPLICATION_MODAL);
            checkoutStage.setTitle("NFT Checkout");
            checkoutStage.setScene(new Scene(root));
            checkoutStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error opening checkout page: " + e.getMessage());
        }

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
        loadPageWithUserId(event,"Customer/customerMessengerPage.fxml");

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