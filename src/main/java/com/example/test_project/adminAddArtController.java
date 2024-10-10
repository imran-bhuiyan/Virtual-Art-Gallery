package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class adminAddArtController extends  BaseController{

    @FXML private ComboBox<String> catagorySelect;
    @FXML private TextField imagePathField;
    @FXML private TextField photoname;
    @FXML private TextField photoprice;
    @FXML private TextField photoyear;
    @FXML private TextField artistID;

    public void initialize() {
        if (catagorySelect != null) {
            catagorySelect.getItems().addAll("Abstract", "Landscape", "Portrait", "Still Life", "Modern", "Contemporary");

        }
    }



    @FXML
    void photoBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) imagePathField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            imagePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    void photoadd(ActionEvent event) {
        String idStr = artistID.getText();
        if (idStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Artist ID is required and must be an integer.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Artist ID must be a valid integer.");
            return;
        }


        // Check if the artist ID is valid and exists in the database
        if (!isValidArtistId(id)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Artist ID does not exist or is not associated with an artist.");
            return;
        }

        String name = photoname.getText();
        String yearStr = photoyear.getText();
        String category = catagorySelect.getValue();
        String priceStr = photoprice.getText();
        String imagePath = imagePathField.getText();

        if (name.isEmpty() || yearStr.isEmpty() || category == null || priceStr.isEmpty() || imagePath.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
            return;
        }


        int year;
        double price;
        try {
            year = Integer.parseInt(yearStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid year or price format");
            return;
        }

        String newImagePath = copyImageToDesignatedFolder(imagePath);
        if (newImagePath == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to copy the image");
            return;
        }

        try (Connection conn = DataBaseConnection.getConnection()) {
            String query = "INSERT INTO Paintings (name, artist_id, year, category, price, image_url) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, id);
                pstmt.setInt(3, year);
                pstmt.setString(4, category);
                pstmt.setDouble(5, price);
                pstmt.setString(6, newImagePath);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Painting added successfully");
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add painting");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the painting: " + e.getMessage());
        }
    }

    private boolean isValidArtistId(int artistId) {
        String query = "SELECT COUNT(*) FROM users WHERE user_id = ? AND role = 'artist'";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private String copyImageToDesignatedFolder(String sourcePath) {
        try {
            String originalFileName = Paths.get(sourcePath).getFileName().toString();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            String destinationFolder = "uploads/paintings/";
            Path destinationPath = Paths.get(destinationFolder, uniqueFileName);

            Files.createDirectories(destinationPath.getParent());
            Files.copy(Paths.get(sourcePath), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Image copied successfully to: " + destinationPath.toString());

            return destinationFolder + uniqueFileName;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error copying image: " + e.getMessage());
            return null;
        }
    }

    private void clearFields() {
        photoname.clear();
        photoyear.clear();
        catagorySelect.getSelectionModel().clearSelection();
        photoprice.clear();
        imagePathField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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