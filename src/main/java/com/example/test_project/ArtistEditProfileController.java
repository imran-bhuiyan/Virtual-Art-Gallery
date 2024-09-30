package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.regex.Pattern;

public class ArtistEditProfileController {

    @FXML private TextField emailField;
    @FXML private TextField nameField;

    public void initialize() {
        loadProfileData();
    }

    public void setArtistId(int artistId) {
        System.out.println("artisId From Artist Edit Profile Controller : " + artistId);
        CurrentArtist.getInstance().setArtistId(artistId);
        loadProfileData();
    }

    @FXML void artistAddAuctions(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddAuction.fxml");
    }

    @FXML void artistAddPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddPaint.fxml");
    }

    @FXML void artistDashboard(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistDashboard.fxml");
    }

    @FXML void artistLogout(ActionEvent event) throws IOException {
        loadPage(event, "Guest/userOrGuestHomePage.fxml");
    }

    @FXML void artistMessages(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMessage.fxml");
    }

    @FXML void artistMyPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMyPaintPage.fxml");
    }

    @FXML void artistMyProfile(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistProfile.fxml");
    }

    @FXML void artistPaintings(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistPaintingPage.fxml");
    }

    private void loadProfileData() {
        int artistId = CurrentArtist.getInstance().getArtistId();
        String query = "SELECT name, email FROM Users WHERE user_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                emailField.setText(rs.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to load profile data.");
        }
    }

    @FXML
    void handleSubmit(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            showAlert(AlertType.WARNING, "Invalid Input", "Name and email cannot be empty.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(AlertType.WARNING, "Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (isEmailExists(email)) {
            showAlert(AlertType.WARNING, "Email Exists", "This email is already in use. Please use a different email.");
            return;
        }

        updateProfile(name, email);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        return pat.matcher(email).matches();
    }

    private boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM Users WHERE email = ? AND user_id != ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setInt(2, CurrentArtist.getInstance().getArtistId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to check email existence.");
        }
        return false;
    }

    private void updateProfile(String name, String email) {
        String query = "UPDATE Users SET name = ?, email = ? WHERE user_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setInt(3, CurrentArtist.getInstance().getArtistId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(AlertType.INFORMATION, "Success", "Profile updated successfully.");
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to update profile.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Failed to update profile.");
        }
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof ArtistProfileController) {
            ((ArtistProfileController) controller).setArtistId(CurrentArtist.getInstance().getArtistId());
        }

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}