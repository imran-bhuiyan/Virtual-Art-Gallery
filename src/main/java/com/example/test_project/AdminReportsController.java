package com.example.test_project;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AdminReportsController {

    @FXML
    private VBox reportsContainer;

    private List<Report> reports = Arrays.asList(
            new Report("5007", "Kibutsuji Muzan", "Issue with artwork delivery"),
            new Report("5008", "Tanjiro Kamado", "Request for refund"),
            new Report("5009", "Nezuko Kamado", "Complaint about event organization")
    );

    @FXML
    public void initialize() {
        loadReports();
    }

    private void loadReports() {
        for (Report report : reports) {
            reportsContainer.getChildren().add(createReportPane(report));
        }
    }

    private Pane createReportPane(Report report) {
        Pane pane = new Pane();
        pane.setPrefSize(668, 100);
        pane.setStyle("-fx-background-color: #D4F1F9; -fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");

        Text idText = new Text("ID: " + report.getId());
        idText.setLayoutX(15);
        idText.setLayoutY(31);

        Text nameText = new Text("Name: " + report.getName());
        nameText.setLayoutX(18);
        nameText.setLayoutY(55);

        Text descText = new Text("Description: " + report.getDescription());
        descText.setLayoutX(18);
        descText.setLayoutY(77);

        TextField responseField = new TextField();
        responseField.setLayoutX(345);
        responseField.setLayoutY(38);

        Text responseLabel = new Text("Send Response:");
        responseLabel.setLayoutX(252);
        responseLabel.setLayoutY(55);

        CheckBox doneCheck = new CheckBox("Done");
        doneCheck.setLayoutX(584);
        doneCheck.setLayoutY(39);

        pane.getChildren().addAll(idText, nameText, descText, responseField, responseLabel, doneCheck);

        return pane;
    }

    // Navigation methods
    @FXML
    private void handleDashboard() {
        System.out.println("Navigate to Dashboard");
    }

    @FXML
    private void handleAddArtist() {
        System.out.println("Navigate to Add Artist");
    }

    @FXML
    private void handleManageArtists() {
        System.out.println("Navigate to Manage Artists");
    }

    @FXML
    private void handleAddArtwork() {
        System.out.println("Navigate to Add Artwork");
    }

    @FXML
    private void handleManageArtworks() {
        System.out.println("Navigate to Manage Artworks");
    }

    @FXML
    private void handleAddCustomer() {
        System.out.println("Navigate to Add Customer");
    }

    @FXML
    private void handleManageCustomers() {
        System.out.println("Navigate to Manage Customers");
    }

    @FXML
    private void handleOrders() {
        System.out.println("Navigate to Orders");
    }

    @FXML
    private void handleAddEvent() {
        System.out.println("Navigate to Add Event");
    }

    @FXML
    private void handleManageEvents() {
        System.out.println("Navigate to Manage Events");
    }

    @FXML
    private void handleChats() {
        System.out.println("Navigate to Chats");
    }

    @FXML
    private void handleReports() {
        System.out.println("Navigate to Reports");
    }

    @FXML
    private void handleInquiries() {
        System.out.println("Navigate to Inquiries");
    }

    @FXML
    private void handleAuctions() {
        System.out.println("Navigate to Auctions");
    }

    @FXML
    private void handleLogout() {
        System.out.println("Perform logout");
    }
    @FXML
    void goBack(ActionEvent event)throws IOException {
        loadPage(event, "Guest/userOrGuestHomePage.fxml");
    }
    void sendResponse(ActionEvent event)throws IOException {
        loadPage(event, "Guest/userOrGuestHomePage.fxml");
    }

    // Helper class to represent a report
    private static class Report {
        private String id;
        private String name;
        private String description;

        public Report(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    private void loadPage(javafx.event.ActionEvent event, String fxmlFile) {
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
}