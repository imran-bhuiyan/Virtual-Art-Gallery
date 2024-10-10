package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class guestPageController {

    @FXML
    private void goHome(ActionEvent event) throws IOException {
        // Already on home page, so no action needed
        loadPage(event,"guest/userOrGuestHomePage.fxml");
    }

    @FXML
    private void goEvents(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestEventPage.fxml");
    }

    @FXML
    private void goPaintings(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestPaintingsPage.fxml");
    }

    @FXML
    private void goTopArt(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestTopArtPage.fxml");
    }

    @FXML
    private void goContactUS(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestContactUsPage.fxml");

    };

    @FXML
    private void goLogin(ActionEvent event) throws IOException {
        loadPage(event, "customer/customerLoginPage.fxml");
    }

    @FXML
    private void goSignUpChoice(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestRegistrationForm.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}