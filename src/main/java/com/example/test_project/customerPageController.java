package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class customerPageController {

    @FXML
    void goCustomerAuction(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerLiveAuctionPage.fxml");

    }

    @FXML
    void goCustomerCart(ActionEvent event) throws IOException {
        loadPage(event,"Customer/customerPaintingCheckout.fxml");

    }

    @FXML
    void goCustomerEvents(ActionEvent event) throws IOException {
        loadPage(event,"Customer/customerEventPage.fxml");

    }

    @FXML
    void goCustomerHome(ActionEvent event) throws IOException {
        loadPage(event,"Customer/customerHomePage.fxml");

    }

    @FXML
    void goCustomerMessages(ActionEvent event) throws IOException {
        loadPage(event,"Customer/customerMessengerPage.fxml");

    }

    @FXML
    void goCustomerNFT(ActionEvent event) throws IOException {
        loadPage(event,"Customer/customerNFTpage.fxml");

    }

    @FXML
    void goCustomerNotification(ActionEvent event) throws IOException {
        loadPage(event,"Customer/customerNotification.fxml");

    }

    @FXML
    void goCustomerPaintings(ActionEvent event) throws IOException {
        loadPage(event,"Customer/customerPaintingPage.fxml");

    }

    @FXML
    void goCustomerProfile(ActionEvent event) throws IOException {
        loadPage(event,"Customer/customerProfilePage.fxml");

    }

    @FXML
    void goCustomerTopArt(ActionEvent event) throws IOException {
        loadPage(event,"Customer/customerPageTopArt.fxml");

    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
