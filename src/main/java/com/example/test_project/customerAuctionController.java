package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class customerAuctionController extends BaseController {

    @FXML
    private VBox auctionItemsContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();
    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Uploads";

    @FXML
    public void initialize() {
//        loadAuctionItems();
    }

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("customerAuctionController: setUserId() called with userId: " + userId);
        loadAuctionItems();
    }

    private void loadAuctionItems() {
        auctionItemsContainer.getChildren().clear();
        String query = "SELECT p.name as Title, u.name as Artist, p.category as Category, " +
                "a.starting_bid, a.current_bid, a.ends_time, a.start_date, a.auction_id, p.image_url " +
                "FROM auctions as a JOIN paintings as p ON a.painting_id = p.painting_id " +
                "JOIN users as u ON u.user_id = p.artist_id WHERE a.status = 'active' or a.status = 'pending'";


        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                AnchorPane itemPane = createAuctionItemPane(rs);
                auctionItemsContainer.getChildren().add(itemPane);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load auction items: " + e.getMessage());
        }
    }

    private AnchorPane createAuctionItemPane(ResultSet rs) throws SQLException {
        AnchorPane pane = new AnchorPane();
        pane.setPrefHeight(270.0);
        pane.setPrefWidth(816.0);
        pane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");

        ImageView paintingImageView = new ImageView();
        paintingImageView.setFitWidth(150);
        paintingImageView.setFitHeight(150);
        paintingImageView.setLayoutX(20);
        paintingImageView.setLayoutY(20);

        String imageUrl = rs.getString("image_url");
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

        Text title = createText(rs.getString("Title"), 220, 30, "-fx-font-size: 18px; -fx-font-weight: bold;");
        Text artist = createText("Artist: " + rs.getString("Artist"), 220, 60, null);
        Text category = createText("Category: " + rs.getString("Category"), 220, 90, null);
        Text startingBid = createText("Starting Bid: $" + rs.getDouble("starting_bid"), 220, 120, null);
        Text currentBid = createText("Current Bid: $" + rs.getDouble("current_bid"), 220, 150, null);
        Text endsTime = createText("Auction Ends: " + rs.getString("ends_time"), 220, 180, null);

        Text startTime = createText("Auction Starts: " + rs.getString("start_date"), 220, 200, null);

        // Convert timestamps to Java LocalDateTime
        LocalDateTime startDate = rs.getTimestamp("start_date").toLocalDateTime();
        LocalDateTime endsDate = rs.getTimestamp("ends_time").toLocalDateTime();
        LocalDateTime currentDate = LocalDateTime.now();

        Text auctionStatus = new Text();
        auctionStatus.setLayoutX(650);
        auctionStatus.setLayoutY(30);
        auctionStatus.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button bidButton = new Button("Place Bid");
        bidButton.setLayoutX(380);
        bidButton.setLayoutY(240);
        bidButton.setStyle("-fx-background-color: #4CAF50;");
        bidButton.setDisable(true);  // Disable by default

        if (currentDate.isBefore(startDate)) {
            auctionStatus.setText("COMING SOON");
            auctionStatus.setStyle("-fx-text-fill: #FFA500; -fx-font-weight: bold; -fx-font-size: 16px;");
        } else if (currentDate.isAfter(startDate) && currentDate.isBefore(endsDate)) {
            auctionStatus.setText("LIVE NOW!");
            auctionStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 18px;");
            bidButton.setDisable(false);  // Enable the button since the auction is live
        } else if (currentDate.isAfter(endsDate)) {
            auctionStatus.setText("AUCTION ENDED");
            auctionStatus.setStyle("-fx-text-fill: #808080; -fx-font-weight: bold; -fx-font-size: 16px;");
        }



        TextField bidField = new TextField();
        bidField.setLayoutX(220);
        bidField.setLayoutY(240);
        bidField.setPromptText("Enter your bid");

        int auctionId = rs.getInt("auction_id");
        bidButton.setOnAction(event -> placeBid(auctionId, bidField.getText(), startingBid, currentBid));

        pane.getChildren().addAll(paintingImageView, title, artist, category, startingBid, currentBid, endsTime, startTime, auctionStatus, bidField, bidButton);

        return pane;
    }


    private void setPlaceholderImage(ImageView imageView) {
        File placeholderFile = new File(IMAGE_DIRECTORY, "placeholder.png");
        if (placeholderFile.exists()) {
            imageView.setImage(new Image(placeholderFile.toURI().toString()));
        } else {
            System.out.println("Placeholder image not found");
        }
    }

    private Text createText(String content, double x, double y, String style) {
        Text text = new Text(content);
        text.setLayoutX(x);
        text.setLayoutY(y);
        if (style != null) {
            text.setStyle(style);
        }
        return text;
    }

    private void placeBid(int auctionId, String bidAmount, Text startingBidText, Text currentBidText) {
        try {
            double newBid = Double.parseDouble(bidAmount);
            double currentBid = Double.parseDouble(currentBidText.getText().replaceAll("[^\\d.]", ""));
            double startingBid = Double.parseDouble(startingBidText.getText().replaceAll("[^\\d.]", ""));

            if (newBid <= currentBid || newBid <= startingBid) {
                showAlert("Invalid Bid", "Your bid must be higher than both the starting bid and the current bid.");
                return;
            }

            // Check user's balance
            Connection conn = dbConnection.getConnection();
            String checkBalanceQuery = "SELECT credits FROM users WHERE user_id = ?";
            try (PreparedStatement balanceStmt = conn.prepareStatement(checkBalanceQuery)) {
                balanceStmt.setInt(1, this.userId);
                ResultSet rs = balanceStmt.executeQuery();
                if (rs.next()) {
                    double userCredits = rs.getDouble("credits");
                    if (userCredits < newBid) {
                        showAlert("Insufficient Balance", "You don't have enough credits to place this bid.");
                        return;
                    }
                }
            }

            conn.setAutoCommit(false);

            try {
                // Update current_bid in auctions table
                String updateAuctionQuery = "UPDATE auctions SET current_bid = ? WHERE auction_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateAuctionQuery)) {
                    pstmt.setDouble(1, newBid);
                    pstmt.setInt(2, auctionId);
                    pstmt.executeUpdate();
                }

                // Insert bid into bids table
                String insertBidQuery = "INSERT INTO bids (auction_id, user_id, bid_amount, bid_time) VALUES (?, ?, ?, NOW())";
                try (PreparedStatement pstmt = conn.prepareStatement(insertBidQuery)) {
                    pstmt.setInt(1, auctionId);
                    pstmt.setInt(2, this.userId);
                    pstmt.setDouble(3, newBid);
                    pstmt.executeUpdate();
                }

                conn.commit();
                showConfirmation("Bid Placed Successfully", "Your bid of $" + newBid + " has been placed.");
                currentBidText.setText("Current Bid: $" + newBid);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for your bid.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to place bid: " + e.getMessage());
        }
    }




    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showConfirmation(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Reload the auction items
                auctionItemsContainer.getChildren().clear();
                loadAuctionItems();
            }
        });
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
        System.out.println("customerAuction : Logging out user");
        // Clear any user-specific data or session information here if needed

        // Navigate to the login page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/customerLoginPage.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

        System.out.println("customerAuctionController: Navigated to login page");

    }


    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("customerAuctionController: loadPageWithUserId() called with path: " + fxmlPath);
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