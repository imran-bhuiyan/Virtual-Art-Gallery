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

    @FXML
    void handleReports(ActionEvent event) throws IOException {
        System.out.println("Admin Reports button clicked");
        loadPage(event, "Admin/AdminReports.fxml");
    }

    @FXML
    void handleAddArtist(ActionEvent event) throws IOException {
        System.out.println("Navigating to Add Artist page");
        loadPage(event, "Admin/AdminAddArtist.fxml");
    }

    @FXML
    void handleAddArtwork(ActionEvent event) throws IOException {
        System.out.println("Navigating to Add Artwork page");
        loadPage(event, "Admin/AdminAddArtwork.fxml");
    }

    @FXML
    void handleAddCustomers(ActionEvent event) throws IOException {
        System.out.println("Navigating to Add Customers page");
        loadPage(event, "Admin/AdminAddCustomer.fxml");
    }

    @FXML
    void handleAddEvents(ActionEvent event) throws IOException {
        System.out.println("Navigating to Add Events page");
        loadPage(event, "Admin/AdminAddEvent.fxml");
    }

    @FXML
    void handleAuctions(ActionEvent event) throws IOException {
        System.out.println("Navigating to Auctions page");
        loadPage(event, "Admin/AdminAuction.fxml");
    }

    @FXML
    void handleChats(ActionEvent event) throws IOException {
        System.out.println("Navigating to Chats page");
        loadPage(event, "Admin/AdminMessages.fxml");
    }

    @FXML
    void handleDashboard(ActionEvent event) throws IOException {
        System.out.println("Navigating to Dashboard page");
        loadPage(event, "Admin/AdminDashboard.fxml");
    }

    @FXML
    void handleInquiries(ActionEvent event) throws IOException {
        System.out.println("Navigating to Inquiries page");
        loadPage(event, "Admin/AdminInquiries.fxml");
    }

    @FXML
    void handleManageArtist(ActionEvent event) throws IOException {
        System.out.println("Navigating to Manage Artist page");
        loadPage(event, "Admin/AdminManageArtist.fxml");
    }

    @FXML
    void handleManageArtwork(ActionEvent event) throws IOException {
        System.out.println("Navigating to Manage Artwork page");
        loadPage(event, "Admin/AdminManageArtwork.fxml");
    }

    @FXML
    void handleManageCustomers(ActionEvent event) throws IOException {
        System.out.println("Navigating to Manage Customers page");
        loadPage(event, "Admin/AdminManageCustomers.fxml");
    }

    @FXML
    void handleManageEvents(ActionEvent event) throws IOException {
        System.out.println("Navigating to Manage Events page");
        loadPage(event, "Admin/AdminManageEvent.fxml");
    }

    @FXML
    void handleOrders(ActionEvent event) throws IOException {
        System.out.println("Navigating to Orders page");
        loadPage(event, "Admin/AdminOrders.fxml");
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        System.out.println("Logging out");
        loadPage(event, "userOrGuestHomePage.fxml");
    }

    // Improved loadPage method with exception handling
    private void loadPage(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // This will help with debugging if loading fails
        }
    }

    public void handleBrowseButtonAction(ActionEvent event) {
    }
}
