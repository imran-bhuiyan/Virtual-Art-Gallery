package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminPageController {

//    @FXML
//    private Label welcomeText;

//      public void setWelcomeText(String username) {
//          if (welcomeText != null) {
//            welcomeText.setText("Welcome, " + username + "!");
//            } else {
//              System.out.println("Welcome text label is null. Make sure it's properly initialized in the FXML file.");
//           }
//        }

//    @FXML
//    void adminAddAuctions(ActionEvent event) throws IOException {
//        loadPage(event, "Admin/AdminAddAuction.fxml");
//    }

    @FXML
    void adminReports(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminReports.fxml");
    }

    @FXML
    void adminAddArtist(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminAddArtist.fxml");
    }

    @FXML
    void adminAddArtwork(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminAddArtwork.fxml");
    }

    @FXML
    void adminAddCustomers(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminAddCustomer.fxml");
    }

    @FXML
    void adminAddEvents(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminAddEvent.fxml");
    }

    @FXML
    void adminAuctions(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminAuction.fxml");
    }

    @FXML
    void adminChats(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminChat.fxml");
    }

    @FXML
    void adminDashboard(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminDashboard.fxml");
    }

    @FXML
    void adminInquiries(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminInquiries.fxml");
    }

    @FXML
    void adminManageArtist(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminManageArtist.fxml");
    }

    @FXML
    void adminManageArtwork(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminManageArtwork.fxml");
    }

    @FXML
    void adminManageCustomers(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminManageCustomers.fxml");
    }

    @FXML
    void adminManageEvents(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminManageEvent.fxml");
    }

    @FXML
    void adminOrders(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminOrders.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
