package com.example.test_project;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class adminEditArtworkController extends BaseController {

    @FXML
    private ImageView paintingImageView;
    @FXML
    private TextField nameField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField artistNameField;
    @FXML
//    private TextField statusField;

    private int paintingId;
    private String currentImageUrl;
    private Runnable refreshCallback;

    public void setPaintingId(int paintingId) {
        this.paintingId = paintingId;
        loadPaintingDetails();
    }

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    private void loadPaintingDetails() {
        String query = "SELECT p.name, p.year, p.category, p.price, p.image_url, a.name AS artist_name " +
                "FROM Paintings p JOIN users a ON p.artist_id = a.user_id WHERE p.painting_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, paintingId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                yearField.setText(String.valueOf(rs.getInt("year")));
                categoryField.setText(rs.getString("category"));
                priceField.setText(String.valueOf(rs.getDouble("price")));
                artistNameField.setText(rs.getString("artist_name"));
//                statusField.setText(rs.getString("status"));
                currentImageUrl = rs.getString("image_url");

                File file = new File(currentImageUrl);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    paintingImageView.setImage(image);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load painting details: " + e.getMessage());
        }
    }

    @FXML
    private void changeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            paintingImageView.setImage(image);
            currentImageUrl = selectedFile.getAbsolutePath();
        }
    }

    @FXML
    private void updatePainting() {
        String query = "UPDATE Paintings SET name = ?, year = ?, category = ?, price = ?, image_url = ? WHERE painting_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nameField.getText());
            pstmt.setInt(2, Integer.parseInt(yearField.getText()));
            pstmt.setString(3, categoryField.getText());
            pstmt.setDouble(4, Double.parseDouble(priceField.getText()));
            pstmt.setString(5, currentImageUrl);
//            pstmt.setString(6, statusField.getText());
            pstmt.setInt(6, paintingId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Painting updated successfully!");
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update painting.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update painting: " + e.getMessage());
        }
    }
//
//    @FXML
//    private void deletePainting() {
//        String query = "DELETE FROM Paintings WHERE painting_id = ?";
//        try (Connection conn = DataBaseConnection.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(query)) {
//
//            pstmt.setInt(1, paintingId);
//
//            int affectedRows = pstmt.executeUpdate();
//            if (affectedRows > 0) {
//                showAlert(Alert.AlertType.INFORMATION, "Success", "Painting deleted successfully!");
//                if (refreshCallback != null) {
//                    refreshCallback.run();
//                }
//                closeWindow();
//            } else {
//                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete painting.");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete painting: " + e.getMessage());
//        }
//    }



    @FXML
    private void cancelEdit() {
        closeWindow();
    }


    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}