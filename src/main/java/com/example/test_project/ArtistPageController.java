package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistPageController {

    @FXML private PasswordField currpass;
    @FXML private PasswordField newpass;
    @FXML private PasswordField currnewpass;
    @FXML private Text db;
    @FXML private Text em;
    @FXML private Text nam;
    @FXML private TextField emailField;
    @FXML private TextField nameField;

    public void initialize() {
        //loadProfileData();
    }

    public void setArtistId(int artistId) {
        System.out.println("artisId From ArtistPageController : " + artistId);
        CurrentArtist.getInstance().setArtistId(artistId);
       // loadProfileData();
    }



    @FXML
    void artistAddAuctions(ActionEvent event) throws IOException {
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
        loadPage(event, "Artist/ArtistProfile.fxml");
    }

    @FXML
    void artistPaintings(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistPaintingPage.fxml");
    }



//    @FXML
//    void editProfile(ActionEvent event) throws IOException {
//        loadPage(event, "Artist/ArtistProfileEdit.fxml");
//    }

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



    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}