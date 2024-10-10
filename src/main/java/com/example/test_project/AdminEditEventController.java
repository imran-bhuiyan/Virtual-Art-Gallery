package com.example.test_project;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminEditEventController extends BaseController {

    @FXML private TextField eventNameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField venueField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField capacityField;
    @FXML private TextField entryFeeField;
    @FXML private ImageView eventImageView;

    private int eventId;
    private String currentImageUrl;

    public void setEventId(int eventId) {
        this.eventId = eventId;
        loadEventDetails();
    }

    private void loadEventDetails() {
        String query = "SELECT * FROM events WHERE event_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                eventNameField.setText(rs.getString("name"));
                descriptionField.setText(rs.getString("description"));
                venueField.setText(rs.getString("venue"));
                datePicker.setValue(rs.getDate("date_time").toLocalDate());
                timeField.setText(rs.getString("time"));
                capacityField.setText(String.valueOf(rs.getInt("capacity")));
                entryFeeField.setText(String.valueOf(rs.getDouble("entry_fee")));
                currentImageUrl = rs.getString("picture_url");

                File file = new File(currentImageUrl);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    eventImageView.setImage(image);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load event details: " + e.getMessage());
        }
    }

    @FXML
    private void changeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            eventImageView.setImage(image);
            currentImageUrl = selectedFile.getAbsolutePath();
        }
    }

    @FXML
    private void updateEvent() {
        String query = "UPDATE events SET name = ?, description = ?, venue = ?, date_time = ?, time = ?, capacity = ?, entry_fee = ?, picture_url = ? WHERE event_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, eventNameField.getText());
            pstmt.setString(2, descriptionField.getText());
            pstmt.setString(3, venueField.getText());
            pstmt.setDate(4, java.sql.Date.valueOf(datePicker.getValue()));
            pstmt.setString(5, timeField.getText());
            pstmt.setInt(6, Integer.parseInt(capacityField.getText()));
            pstmt.setDouble(7, Double.parseDouble(entryFeeField.getText()));
            pstmt.setString(8, currentImageUrl);
            pstmt.setInt(9, eventId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Event updated successfully!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update event.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update event: " + e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        eventNameField.clear();
        descriptionField.clear();
        venueField.clear();
        datePicker.setValue(null);
        timeField.clear();
        capacityField.clear();
        entryFeeField.clear();
        eventImageView.setImage(null);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) eventNameField.getScene().getWindow();
        stage.close();
    }
}