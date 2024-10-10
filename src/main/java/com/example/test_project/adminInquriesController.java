package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class adminInquriesController extends BaseController  {

    @FXML
    private VBox queriesContainer;

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("ArtistDashboardController: userId set to " + userId);
        loadQueries();
    }



    private void loadQueries() {
        String query = "SELECT * FROM contactus where status ='pending' ORDER BY submitted_at DESC";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                HBox queryCard = createQueryCard(
                        rs.getInt("contact_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("message"),
                        rs.getTimestamp("submitted_at").toLocalDateTime(),
                        rs.getString("status")
                );
                queriesContainer.getChildren().add(queryCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private HBox createQueryCard(int contactId, String name, String email, String message, java.time.LocalDateTime submittedAt, String status) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(700);
        card.setPrefHeight(150);

        VBox details = new VBox(8);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label nameLabel = new Label("Name: " + name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label emailLabel = new Label("Email: " + email);
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        Label messageLabel = new Label("Message: " + message);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-wrap-text: true;");

        Label submittedAtLabel = new Label("Submitted: " + submittedAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        submittedAtLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");

        Label statusLabel = new Label("Status: " + status);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (status.equalsIgnoreCase("done") ? "#4CAF50" : "#FFA500") + ";");

        details.getChildren().addAll(nameLabel, emailLabel, messageLabel, submittedAtLabel, statusLabel);

        Button markAsDoneBtn = new Button("Mark as Done");
        markAsDoneBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 16; -fx-cursor: hand;");
        markAsDoneBtn.setOnAction(e -> markAsDone(contactId));
        markAsDoneBtn.setDisable(status.equalsIgnoreCase("done"));

        VBox buttonContainer = new VBox();
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        buttonContainer.getChildren().add(markAsDoneBtn);

        card.getChildren().addAll(details, buttonContainer);
        return card;
    }

    private void markAsDone(int contactId) {
        String updateQuery = "UPDATE contactus SET status = 'done' WHERE contact_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setInt(1, contactId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Refresh the queries display
                queriesContainer.getChildren().clear();
                loadQueries();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

        // Navigation methods
        @FXML
        void goLogout(ActionEvent event) throws IOException {
            loadPage(event, "Customer/customerLoginPage.fxml");
        }

        @FXML
        void goDashboard(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminDashboard.fxml");
        }

        @FXML
        void addArtist(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminAddArtist.fxml");
        }

        @FXML
        void addArtworks(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminAddArtwork.fxml");
        }

        @FXML
        void addEvent(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminAddEvent.fxml");
        }

        @FXML
        void addCustomer(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminAddCustomers.fxml");
        }

        @FXML
        void manageCustomer(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminManageCustomers.fxml");
        }

        @FXML
        void manageArtist(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminManageArtist.fxml");
        }

        @FXML
        void manageArtwork(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminManageArtwork.fxml");
        }

        @FXML
        void manageEvent(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminManageEvent.fxml");
        }

        @FXML
        void goOrders(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminOrderPage.fxml");
        }

        @FXML
        void goMessages(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminMessages.fxml");
        }

        @FXML
        void goQueries(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminQueriesPage.fxml");
        }

        @FXML
        void goAuctionRequest(ActionEvent event) throws IOException {
            loadPageWithUserId(event, "Admin/AdminAuctionPage.fxml");
        }


        private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
            System.out.println("AdminArtistAddController: loadPageWithUserId() called with path: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            BaseController controller = loader.getController();
            if (controller != null) {
                System.out.println("AdminArtistAddController: Setting userId " + userId + " on new controller");
                controller.setUserId(this.userId);
            } else {
                System.out.println("AdminArtistAddController: Warning - controller is null");
            }

            Scene scene = new Scene(root);
            Stage stage;

            if (event.getSource() instanceof MenuItem) {
                // If the event source is a MenuItem, we need to get the stage differently
                MenuItem menuItem = (MenuItem) event.getSource();
                stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
            } else if (event.getSource() instanceof Node) {
                // If it's a regular Node (like a Button), we can get the stage as before
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                throw new IllegalArgumentException("Event source not recognized");
            }

            stage.setScene(scene);
            stage.show();
        }
    }
