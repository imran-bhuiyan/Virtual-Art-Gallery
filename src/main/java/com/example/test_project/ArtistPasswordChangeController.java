package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistPasswordChangeController {

    @FXML
    private PasswordField currnewpass;

    @FXML
    private PasswordField currpass;

    @FXML
    private PasswordField newpass;


        public void setArtistId(int artistId) {
            System.out.println("artisId From Artist Password Change Controller : " + artistId);
        CurrentArtist.getInstance().setArtistId(artistId);

    }





    @FXML
    void artistAddAuctions(ActionEvent event)throws IOException {
        loadPage(event, "Artist/ArtistAddAuction.fxml");

    }

    @FXML
    void artistAddPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddPaint.fxml");
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
       // loadPage(event, "Artist/ArtistProfile.fxml");
    }

    @FXML
    void artistPaintings(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistPaintingPage.fxml");
    }




    @FXML
    void handleChangePassword(ActionEvent event) {
        int artistId = CurrentArtist.getInstance().getArtistId();
        String currentPassword = currpass.getText();
        String newPassword = newpass.getText();
        String confirmNewPassword = currnewpass.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required");
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match");
            return;
        }

        try (Connection conn = DataBaseConnection.getConnection()) {
            String verifyQuery = "SELECT * FROM Users WHERE user_id = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(verifyQuery)) {
                pstmt.setInt(1, artistId);
                pstmt.setString(2, currentPassword);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect");
                    return;
                }
            }

            String updateQuery = "UPDATE Users SET password = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                pstmt.setString(1, newPassword);
                pstmt.setInt(2, artistId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully");
                    clearPasswordFields();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while changing the password");
        }
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof ArtistPageController) {
            ((ArtistPageController) controller).setArtistId(CurrentArtist.getInstance().getArtistId());
        }

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void clearPasswordFields() {
        currpass.clear();
        newpass.clear();
        currnewpass.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}






