//CustomerHomePageController.java
package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class customerHomePageController extends BaseController {

    @FXML
    private TextField balance;



    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("CustomerHomePageController: userId set to " + userId);

    }



    private void loadUserBalance() {
        String query = "SELECT credits FROM users WHERE user_id = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double credits = rs.getDouble("credits");
                balance.setText(String.format("%.2f", credits));
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    @FXML
    public void initialize() {
        loadUserBalance();
    }
    @FXML
    void seeBalance(ActionEvent event) {
        loadUserBalance();
    }

    @FXML
    void addCredit(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/customerAddcredit.fxml"));
        Parent root = loader.load();

        CustomerAddCreditController controller = loader.getController();
        controller.setUserId(this.userId);

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.showAndWait();

        // Reload the balance after the addCredit window is closed
        loadUserBalance();
    }

    @FXML
    void customerLogout(ActionEvent event) throws IOException {
        System.out.println("CustomerHomePage : Logging out user");
        // Clear any user-specific data or session information here if needed

        // Navigate to the login page
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/customerLoginPage.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

        System.out.println("CustomerHomePage: Navigated to login page");

    }

    @FXML
    void goCustomerAuction(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerLiveAuctionPage.fxml");

    }

    @FXML
    void goCustomerCart(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerPaintingCheckout.fxml");

    }

    @FXML
    void goCustomerEvents(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerEventPage.fxml");

    }

    @FXML
    void goCustomerHome(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerHomePage.fxml");

    }

    @FXML
    void goCustomerMessages(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerMessengerPage.fxml");

    }

    @FXML
    void goCustomerNFT(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerNFTpage.fxml");

    }

    @FXML
    void goCustomerNotification(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerNotification.fxml");

    }

    @FXML
    void goCustomerPaintings(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerPaintingPage.fxml");

    }

    @FXML
    void goCustomerProfile(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerProfilePage.fxml");

    }

    @FXML
    void goCustomerTopArt(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Customer/customerPageTopArt.fxml");

    }

    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("ArtistDashboardController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("CustomerHomePageController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("CustomerHomePageController: Warning - controller is null");
        }

        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

}
