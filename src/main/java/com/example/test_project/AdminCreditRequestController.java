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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminCreditRequestController extends BaseController {

    @FXML
    private VBox creditRequestsContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();

    @FXML
    public void initialize() {
        loadCreditRequests();
    }

    private void loadCreditRequests() {
        creditRequestsContainer.getChildren().clear();
        String query = "SELECT uc.id, uc.user_id, uc.amount, uc.trnxId, uc.contact, uc.d_status, u.name " +
                "FROM user_credit uc JOIN users u ON uc.user_id = u.user_id " +
                "WHERE uc.d_status = 'pending'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                creditRequestsContainer.getChildren().add(createCreditRequestCard(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getDouble("amount"),
                        rs.getString("trnxId"),
                        rs.getString("contact"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    private Node createCreditRequestCard(int requestId, int userId, double amount, String trnxId, String contact, String username) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPrefWidth(350);
        card.setStyle("-fx-background-color: #ffffff;" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-width: 0 0 1 0;" +
                "-fx-padding: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        // Username
        Text usernameText = new Text("Username: " + username);
        usernameText.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Amount
        Text amountText = new Text("Amount: $" + String.format("%.2f", amount));
        amountText.setStyle("-fx-font-size: 14px;");

        // Transaction ID
        Text trnxIdText = new Text("Transaction ID: " + trnxId);
        trnxIdText.setStyle("-fx-font-size: 14px;");

        // Contact
        Text contactText = new Text("Contact: " + contact);
        contactText.setStyle("-fx-font-size: 14px;");

        // Buttons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button acceptButton = new Button("Accept");
        acceptButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        acceptButton.setOnAction(e -> handleCreditRequest(requestId, userId, amount, true));

        Button declineButton = new Button("Decline");
        declineButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        declineButton.setOnAction(e -> handleCreditRequest(requestId, userId, amount, false));

        buttonBox.getChildren().addAll(acceptButton, declineButton);

        card.getChildren().addAll(usernameText, amountText, trnxIdText, contactText, buttonBox);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-background-color: #f5f5f5;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle() + "-fx-background-color: #ffffff;"));

        return card;
    }

    private void handleCreditRequest(int requestId, int userId, double amount, boolean isAccepted) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            String notificationMessage;
            String notificationTitle;

            if (isAccepted) {
                // Update user_credit table
                String updateCreditQuery = "UPDATE user_credit SET d_status = 'approved' WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateCreditQuery)) {
                    pstmt.setInt(1, requestId);
                    pstmt.executeUpdate();
                }

                // Update users table (credits)
                String updateUserQuery = "UPDATE users SET credits = credits + ? WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateUserQuery)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                }

                notificationTitle = "Credit Request Approved";
                notificationMessage = "Your credit request for $" + String.format("%.2f", amount) + " has been approved.";
            } else {
                // Update user_credit table to decline the request
                String declineQuery = "UPDATE user_credit SET d_status = 'declined' WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(declineQuery)) {
                    pstmt.setInt(1, requestId);
                    pstmt.executeUpdate();
                }

                notificationTitle = "Credit Request Declined";
                notificationMessage = "Your credit request for $" + String.format("%.2f", amount) + " has been declined.";
            }

            // Insert notification for the user
            String insertNotificationQuery = "INSERT INTO notifications (user_id, type, title, message, created_at) VALUES (?, ?, ?, ?, NOW())";
            try (PreparedStatement pstmt = conn.prepareStatement(insertNotificationQuery)) {
                pstmt.setInt(1, userId);  // user_id who made the request
                pstmt.setString(2, "credit_request"); // type of the notification
                pstmt.setString(3, notificationTitle); // notification title
                pstmt.setString(4, notificationMessage); // notification message
                pstmt.executeUpdate();
            }

            conn.commit();
            showAlert(isAccepted ? "Credit request approved" : "Credit request declined");
            loadCreditRequests(); // Reload the requests after processing
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String message) {

        System.out.println("Alert: " + message);
        // TODO: Implement actual alert dialog
    }

    // Navigation methods

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
    void goAuctionRequest(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminAuctionPage.fxml");
    }

    @FXML
    void goDashboard(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminDashboard.fxml");

    }

    @FXML
    void goLogout(ActionEvent event) throws IOException {

        loadPage(event, "Customer/customerLoginPage.fxml");



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
    void goMessages(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminMessages.fxml");


    }

    @FXML
    void goOrders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminOrderPage.fxml");

    }

    @FXML
    void goQueries(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminQueriesPage.fxml");

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
    void goCreditRequest(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Admin/AdminCreditRequest.fxml");

    }

    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("AdminCreditRequestController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("AdminCreditRequestController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("AdminCreditRequestController: Warning - controller is null");
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