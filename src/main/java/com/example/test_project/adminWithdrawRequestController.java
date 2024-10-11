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
import java.sql.*;
import java.time.LocalDateTime;

public class adminWithdrawRequestController extends BaseController {

    @FXML
    private VBox withdrawRequestsContainer;

    private DataBaseConnection dbConnection = new DataBaseConnection();

    @FXML
    public void initialize() {
        loadWithdrawRequests();
    }

    private void loadWithdrawRequests() {
        withdrawRequestsContainer.getChildren().clear();
        String query = "SELECT wr.user_id, wr.amount, wr.account_no, wr.payment_method, wr.status, wr.request_date, u.name " +
                "FROM withdraw_credit wr JOIN users u ON wr.user_id = u.user_id " +
                "WHERE wr.status = 'pending'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                withdrawRequestsContainer.getChildren().add(createWithdrawRequestCard(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getDouble("amount"),
                        rs.getString("account_no"),
                        rs.getString("payment_method"),
                        rs.getString("status"),
                        rs.getTimestamp("request_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading withdraw requests");
        }
    }

    private Node createWithdrawRequestCard(int userId, String name, double amount, String accountNo, String paymentMethod, String status, Timestamp requestDate) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPrefWidth(350);
        card.setStyle("-fx-background-color: #ffffff;" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-width: 0 0 1 0;" +
                "-fx-padding: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        Text nameText = new Text("Name: " + name);
        Text idText = new Text("ID: " + userId);
        Text amountText = new Text("Amount: $" + String.format("%.2f", amount));
        Text accountNoText = new Text("Account No: " + accountNo);
        Text paymentMethodText = new Text("Payment Method: " + paymentMethod);
        Text statusText = new Text("Status: " + status);
        Text requestDateText = new Text("Request Time: " + requestDate);

        // Buttons
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button approveButton = new Button("Approve");
        approveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        approveButton.setOnAction(e -> handleWithdrawRequest(userId, amount, true));

        Button declineButton = new Button("Decline");
        declineButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        declineButton.setOnAction(e -> handleWithdrawRequest(userId, amount, false));

        buttonBox.getChildren().addAll(approveButton, declineButton);

        card.getChildren().addAll(nameText, idText, amountText, accountNoText, paymentMethodText, statusText, requestDateText, buttonBox);

        return card;
    }

    private void handleWithdrawRequest(int userId, double amount, boolean isApproved) {
        String confirmationMessage = isApproved ? "Are you sure you want to approve this withdrawal request?" :
                "Are you sure you want to decline this withdrawal request?";

        if (showConfirmation(confirmationMessage)) {
            try (Connection conn = dbConnection.getConnection()) {
                conn.setAutoCommit(false);

                String updateStatusQuery = "UPDATE withdraw_credit SET status = ?, approve_date = ? WHERE user_id = ? AND status = 'pending'";
                try (PreparedStatement pstmt = conn.prepareStatement(updateStatusQuery)) {
                    pstmt.setString(1, isApproved ? "approved" : "rejected");
                    pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.setInt(3, userId);
                    pstmt.executeUpdate();
                }

                if (!isApproved) {
                    String updateCreditsQuery = "UPDATE users SET credits = credits + ? WHERE user_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateCreditsQuery)) {
                        pstmt.setDouble(1, amount);
                        pstmt.setInt(2, userId);
                        pstmt.executeUpdate();
                    }
                }

                String notificationType = isApproved ? "withdrawal_approved" : "withdrawal_rejected";
                String notificationTitle = isApproved ? "Withdrawal Approved" : "Withdrawal Rejected";
                String notificationMessage = isApproved ?
                        "Your withdrawal request for $" + String.format("%.2f", amount) + " has been approved." :
                        "Your withdrawal request for $" + String.format("%.2f", amount) + " has been rejected. The amount has been credited back to your account.";

                String insertNotificationQuery = "INSERT INTO notifications (user_id, type, title, message) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertNotificationQuery)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, notificationType);
                    pstmt.setString(3, notificationTitle);
                    pstmt.setString(4, notificationMessage);
                    pstmt.executeUpdate();
                }

                conn.commit();
                showAlert(isApproved ? "Withdrawal request approved" : "Withdrawal request declined");
                loadWithdrawRequests(); // Reload the withdraw requests
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error processing withdrawal request");
            }
        }
    }

    private boolean showConfirmation(String message) {
       // will do later if possible
        System.out.println("Confirmation: " + message);
        return true;
    }

    private void showAlert(String message) {
        // will do later if possible
        System.out.println("Alert: " + message);
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
        System.out.println("AdminWithdrawRequestController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("AdminWithdrawRequestController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("AdminWithdrawRequestController: Warning - controller is null");
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