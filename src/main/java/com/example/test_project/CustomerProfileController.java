package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;

public class CustomerProfileController extends BaseController {
    private static final String IMAGE_DIRECTORY = "D:\\Trimester\\8th\\AOOP\\IntellijIdea\\Test_Project\\src\\main\\java\\Uploads";
    private static final String PLACEHOLDER_IMAGE = "placeholder.png";

    private DataBaseConnection dbConnection = new DataBaseConnection();

    @FXML
    private TextField userName, id, userEmail, userPass, userRole, userDOB, userCredit;

    @FXML
    private VBox nftListContainer;

    @FXML
    private ScrollPane nftScrollPane;


    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("CustomerProfileController: setUserId() called with userId: " + userId);
        loadProfileFromDatabase();
        loadNFTCollection();
    }

    private void loadProfileFromDatabase() {
        String query = "SELECT user_id, name, email, password, role, dob, credits " +
                "FROM users  " +
                "WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                userName.setText(rs.getString("name"));
                id.setText(String.valueOf(rs.getInt("user_id")));
                userEmail.setText(rs.getString("email"));
                userPass.setText(rs.getString("password"));
                userRole.setText(rs.getString("role"));
                userDOB.setText(rs.getString("dob"));
                userCredit.setText(String.valueOf(rs.getDouble("credits")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load user profile.");
        }
    }

    @FXML
    void updateButton(ActionEvent event) {
        // Check if any field is empty
        if (userName.getText().trim().isEmpty() || userEmail.getText().trim().isEmpty() || userPass.getText().trim().isEmpty()) {
            showAlert("Error", "All fields must be filled. Please ensure no field is left empty.");
            return;
        }
        // Validate email
        String email = userEmail.getText().trim();
        if (!isValidEmail(email)) {
            showAlert("Error", "Invalid email format. Please enter a valid email address.");
            return;
        }

        // Check if  email is unique
        if (!isUniqueEmail(email)) {
            showAlert("Error", "Email is already in use by another account.");
            return;
        }

        String query = "UPDATE users SET name = ?, email = ?, password = ? WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userName.getText().trim());
            pstmt.setString(2, userEmail.getText().trim());
            pstmt.setString(3, userPass.getText().trim());
            pstmt.setInt(4, userId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert("Success", "Profile updated successfully.");
            } else {
                showAlert("Error", "Failed to update profile. No changes were made.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to update profile: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }


    private boolean isUniqueEmail(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id != ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // Email is unique if count is 0
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to check email uniqueness: " + e.getMessage());
        }
        return false; //   if  error
    }

    @FXML
    void clearButton(ActionEvent event) {
        userName.clear();
        userEmail.clear();
        userPass.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void myNFT(ActionEvent event) {
        loadNFTCollection();
    }

    private void loadNFTCollection() {
        nftListContainer.getChildren().clear();
        String query = "SELECT n.name AS Title, n.edition, n.price, n.image_url, n.Blockchain, u.name AS Artist " +
                "FROM nfts n JOIN users u ON n.artist_id = u.user_id " +
                "WHERE n.owner_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String nftTitle = rs.getString("Title");
                String nftEdition = rs.getString("edition");
                BigDecimal nftPrice = rs.getBigDecimal("price");
                String imageUrl = rs.getString("image_url");
                String artistName = rs.getString("Artist");
                String blockChain = rs.getString("Blockchain");

                Node nftItem = createNFTListItem(nftTitle, nftEdition, nftPrice, imageUrl, artistName,blockChain);
                nftListContainer.getChildren().add(nftItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load NFT collection.");
        }
    }

    private void viewNFTImage(String imageUrl) {
        File imageFile = new File(imageUrl);
        Image image;

        if (imageFile.exists()) {
            try {
                image = new Image(imageFile.toURI().toString());
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
                image = getPlaceholderImage();
            }
        } else {
            System.out.println("Image file not found: " + imageUrl);
            image = getPlaceholderImage();
        }

        // Create a new stage (window)
        Stage newWindow = new Stage();
        newWindow.setTitle("NFT Image");

        // Create an ImageView for displaying the image
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);

        // Set the fixed size for the ImageView (500x600)
        imageView.setFitWidth(500);
        imageView.setFitHeight(600);

        // Add the ImageView to a layout container (StackPane)
        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, 500, 600);

        // Set the scene and show the new window
        newWindow.setScene(scene);
        newWindow.show();
    }
    // Add this helper method to get the placeholder image
    private Image getPlaceholderImage() {
        File placeholderFile = new File(IMAGE_DIRECTORY, PLACEHOLDER_IMAGE);
        if (placeholderFile.exists()) {
            return new Image(placeholderFile.toURI().toString());
        } else {
            System.out.println("Placeholder image not found");
            return null;
        }
    }

    private Node createNFTListItem(String nftTitle, String nftEdition, BigDecimal nftPrice, String imageUrl, String artistName ,String blockChain) {
        // Create main card container with padding and background styling
        VBox card = new VBox(15);  // Increase spacing between elements
        card.setStyle(
                "-fx-background-color: #f0f8ff; " +  // Set light blue background for a cleaner look
                        "-fx-border-color: #a0a0a0; " +  // Soft grey border
                        "-fx-border-radius: 10; " +  // Rounded corners for smoothness
                        "-fx-padding: 20; " +  // Add more padding inside the card
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 12, 0, 4, 4);"  // Soft shadow for depth
        );

        // Title label with larger font and bold text
        Label titleLabel = new Label(nftTitle);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");  // Darker text color for contrast

        // Create a horizontal container for details with spacing and alignment
        HBox detailsBox = new HBox(15);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        // Details labels for edition, price, and artist
        Label editionLabel = new Label("Edition: " + nftEdition);
        editionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");  // Subtle grey text for secondary details

        Label priceLabel = new Label("Price: " + nftPrice.toPlainString() + " "+ blockChain);
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c7a7b;");  // Use color for emphasis

        Label artistLabel = new Label("Artist: " + artistName);
        artistLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        // Add labels to the details box
        detailsBox.getChildren().addAll(editionLabel, priceLabel, artistLabel);

        // Create a horizontal box for buttons with right alignment
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // View button with some styling
        Button viewButton = new Button("View");
        viewButton.setStyle("-fx-background-color: #2c7a7b; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5;");
        viewButton.setOnAction(e -> viewNFTImage(imageUrl));

        // Download button with consistent styling
        Button downloadButton = new Button("Download");
        downloadButton.setStyle("-fx-background-color: #2c7a7b; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5;");
        downloadButton.setOnAction(e -> downloadNFT(nftTitle, imageUrl));

        // Add buttons to the button box
        buttonBox.getChildren().addAll(viewButton, downloadButton);

        // Add all elements to the card container
        card.getChildren().addAll(titleLabel, detailsBox, buttonBox);

        return card;
    }


    private void downloadNFT(String nftTitle, String imagePath) {
        try {
            String fileName = nftTitle.replaceAll("[^a-zA-Z0-9.-]", "_") + ".jpg";
            String downloadPath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + fileName;

            File sourceFile = new File(imagePath);
            if (sourceFile.exists()) {
                // It's a local file
                try (InputStream in = new FileInputStream(sourceFile);
                     OutputStream out = new FileOutputStream(downloadPath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                // It's a web URL
                URL url = new URL(imagePath);
                try (InputStream in = url.openStream();
                     OutputStream out = new FileOutputStream(downloadPath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }

            showAlert("Download Successful", "NFT image has been downloaded to: " + downloadPath);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Download Failed", "Unable to download the NFT image: " + e.getMessage());
        }
    }

//    private void viewNFTDetails(String nftTitle, String nftEdition, double nftPrice, String imageUrl, String artistName) {
//        showAlert("NFT Details",
//                "Title: " + nftTitle + "\n" +
//                        "Edition: " + nftEdition + "\n" +
//                        "Price: $" + String.format("%.2f", nftPrice) + "\n" +
//                        "Artist: " + artistName + "\n" +
//                        "Image URL: " + imageUrl);
//    }

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