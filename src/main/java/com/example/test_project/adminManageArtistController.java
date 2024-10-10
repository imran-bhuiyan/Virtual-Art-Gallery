package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class adminManageArtistController extends BaseController {

    @FXML
    private TextField searchField;

    @FXML
    private VBox artistCardsContainer;

    @FXML
    void searchArtist(ActionEvent event) {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            loadArtists("SELECT * FROM users WHERE role = 'artist' AND name LIKE ?", "%" + searchTerm + "%");
        }
    }

    @FXML
    void seeAllArtists(ActionEvent event) {
        loadArtists("SELECT * FROM users WHERE role = 'artist'", null);
    }

    private void loadArtists(String query, String searchTerm) {
        artistCardsContainer.getChildren().clear();
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (searchTerm != null) {
                pstmt.setString(1, searchTerm);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    HBox card = createArtistCard(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getDate("dob").toLocalDate()
                    );
                    artistCardsContainer.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private HBox createArtistCard(int userId, String name, String email, java.time.LocalDate dob) {
        HBox card = new HBox(10);  // 10 is the spacing between children
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(300);
        card.setMinHeight(150);

        VBox details = new VBox(8);  // 8 is the spacing between children
        details.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label idLabel = new Label("ID: " + userId);
        Label emailLabel = new Label("Email: " + email);
        Label dobLabel = new Label("DOB: " + dob.toString());

        details.getChildren().addAll(nameLabel, idLabel, emailLabel, dobLabel);

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        editButton.setOnAction(e -> openEditArtistPage(userId));

        // Use HBox.setHgrow to allow the details to expand and push the button to the right
        HBox.setHgrow(details, Priority.ALWAYS);

        // Align the edit button to the top-right
        editButton.setAlignment(Pos.TOP_RIGHT);

        card.getChildren().addAll(details, editButton);
        return card;
    }

    private void openEditArtistPage(int artistId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Admin/AdminEditArtist.fxml"));
            Parent root = loader.load();

            adminEditArtistController controller = loader.getController();
            controller.setArtistId(artistId);
            controller.setUserId(this.userId);  // Pass the admin's userId

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            controller.setUserId(this.userId);
        }

        Scene scene = new Scene(root);
        Stage stage;

        if (event.getSource() instanceof MenuItem) {
            MenuItem menuItem = (MenuItem) event.getSource();
            stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
        } else if (event.getSource() instanceof Node) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Event source not recognized");
        }

        stage.setScene(scene);
        stage.show();
    }
}