package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class adminAddEventController extends BaseController {

    @FXML private TextField eventName;
    @FXML private TextField category;
    @FXML private DatePicker date;
    @FXML private TextField time;
    @FXML private TextField venue;
    @FXML private TextField entryFee;
    @FXML private TextField imagePathField;
    @FXML private TextField description;
    @FXML private TextField capacity;

    @FXML
    void browseImage(ActionEvent event) {
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
    void addevent(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        String newImagePath = copyImageToDesignatedFolder(imagePathField.getText());
        if (newImagePath == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to copy the image");
            return;
        }

        try (Connection conn = DataBaseConnection.getConnection()) {
            String query = "INSERT INTO events (name, category, date_time, time, venue, entry_fee, picture_url, description, capacity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, eventName.getText());
                pstmt.setString(2, category.getText());
                pstmt.setDate(3, java.sql.Date.valueOf(date.getValue()));
                pstmt.setString(4, time.getText());
                pstmt.setString(5, venue.getText());
                pstmt.setDouble(6, Double.parseDouble(entryFee.getText()));
                pstmt.setString(7, newImagePath);
                pstmt.setString(8, description.getText());
                pstmt.setInt(9, Integer.parseInt(capacity.getText()));

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event added successfully");
                    clearFields();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add event");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while adding the event: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (eventName.getText().isEmpty() || category.getText().isEmpty() || date.getValue() == null ||
                time.getText().isEmpty() || venue.getText().isEmpty() || entryFee.getText().isEmpty() ||
                imagePathField.getText().isEmpty() || description.getText().isEmpty() || capacity.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
            return false;
        }

        try {
            Double.parseDouble(entryFee.getText());
            Integer.parseInt(capacity.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid entry fee or capacity format");
            return false;
        }

        // Validate time format (you can adjust the regex pattern as needed)
        if (!time.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid time format. Please use HH:mm");
            return false;
        }

        return true;
    }

    private String copyImageToDesignatedFolder(String sourcePath) {
        try {
            String originalFileName = Paths.get(sourcePath).getFileName().toString();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            String destinationFolder = "uploads/events/";
            Path destinationPath = Paths.get(destinationFolder, uniqueFileName);

            Files.createDirectories(destinationPath.getParent());
            Files.copy(Paths.get(sourcePath), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            return destinationFolder + uniqueFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void clearFields() {
        eventName.clear();
        category.clear();
        date.setValue(null);
        time.clear();
        venue.clear();
        entryFee.clear();
        imagePathField.clear();
        description.clear();
        capacity.clear();
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