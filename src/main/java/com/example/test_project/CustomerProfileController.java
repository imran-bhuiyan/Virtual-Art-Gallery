package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;

public class CustomerProfileController extends BaseController {

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
        String query = "SELECT n.name AS Title, n.edition, n.price, n.image_url, u.name AS Artist " +
                "FROM nfts n JOIN users u ON n.artist_id = u.user_id " +
                "WHERE n.owner_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String nftTitle = rs.getString("Title");
                String nftEdition = rs.getString("edition");
                double nftPrice = rs.getDouble("price");
                String imageUrl = rs.getString("image_url");
                String artistName = rs.getString("Artist");

                HBox nftItem = createNFTListItem(nftTitle, nftEdition, nftPrice, imageUrl, artistName);
                nftListContainer.getChildren().add(nftItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load NFT collection.");
        }
    }

    private HBox createNFTListItem(String nftTitle, String nftEdition, double nftPrice, String imageUrl, String artistName) {
        HBox hbox = new HBox(10);
        Label titleLabel = new Label("Title: " + nftTitle);
        Label editionLabel = new Label("Edition: " + nftEdition);
        Label priceLabel = new Label("Price: $" + String.format("%.2f", nftPrice));
        Label artistLabel = new Label("Artist: " + artistName);
        Button viewButton = new Button("View");
        viewButton.setOnAction(e -> viewNFTDetails(nftTitle, nftEdition, nftPrice, imageUrl, artistName));
        Button downloadButton = new Button("Download");
        downloadButton.setOnAction(e -> downloadNFT(nftTitle, imageUrl));
        hbox.getChildren().addAll(titleLabel, editionLabel, priceLabel, artistLabel, viewButton, downloadButton);
        return hbox;
    }

    private void downloadNFT(String nftTitle, String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            String fileName = nftTitle.replaceAll("[^a-zA-Z0-9.-]", "_") + ".jpg";
            String downloadPath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + fileName;

            try (InputStream in = url.openStream();
                 OutputStream out = new FileOutputStream(downloadPath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            showAlert("Download Successful", "NFT image has been downloaded to: " + downloadPath);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Download Failed", "Unable to download the NFT image: " + e.getMessage());
        }
    }

    private void viewNFTDetails(String nftTitle, String nftEdition, double nftPrice, String imageUrl, String artistName) {
        showAlert("NFT Details",
                "Title: " + nftTitle + "\n" +
                        "Edition: " + nftEdition + "\n" +
                        "Price: $" + String.format("%.2f", nftPrice) + "\n" +
                        "Artist: " + artistName + "\n" +
                        "Image URL: " + imageUrl);
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