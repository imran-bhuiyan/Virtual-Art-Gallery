package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
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
import java.sql.SQLException;
import java.util.UUID;

public class ArtistAddPaintController {

    @FXML private ComboBox<String> catagorySelect;
    @FXML private TextField imagePathField;
    @FXML private TextField photoname;
    @FXML private TextField photoprice;
    @FXML private TextField photoyear;

    public void initialize() {
        if (catagorySelect != null) {
            catagorySelect.getItems().addAll("Abstract", "Landscape", "Portrait", "Still Life", "Modern", "Contemporary");

        }
    }

    public void setArtistId(int artistId) {
        System.out.println("artisId From Artist Add Painting Controller: " + artistId);
        CurrentArtist.getInstance().setArtistId(artistId);

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
        int artistId = CurrentArtist.getInstance().getArtistId();
        String name = photoname.getText();
        String yearStr = photoyear.getText();
        String category = catagorySelect.getValue();
        String priceStr = photoprice.getText();
        String imagePath = imagePathField.getText();

        if (name.isEmpty() || yearStr.isEmpty() || category == null || priceStr.isEmpty() || imagePath.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required");
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
                pstmt.setInt(2, artistId);
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
    void artistAddAuctions(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddAuction.fxml");
    }

    @FXML
    void artistDashboard(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistDashboard.fxml");
    }

    @FXML
    void artistLogout(ActionEvent event) throws IOException {
        loadPage(event, "Guest/userOrGuestHomePage.fxml");
    }

    @FXML
    void artistMessages(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMessage.fxml");
    }

    @FXML
    void artistMyPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMyPaintPage.fxml");
    }

    @FXML
    void artistMyProfile(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistProfile.fxml");
    }

    @FXML
    void artistPaintings(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistPaintingPage.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}