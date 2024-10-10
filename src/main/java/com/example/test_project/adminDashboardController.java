package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class adminDashboardController extends  BaseController {



    @FXML
    private Text artistCount;

    @FXML
    private Text artworkCount;

    @FXML
    private Text customerCount;

    @FXML
    private Text eventCount;

    @FXML
    private Text inqueriesCount;

    @FXML
    private Text totalSellCount;

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("ArtistDashboardController: userId set to " + userId);
        loadDashboardData();
    }

    public int getUserId() {
        return userId;
    }
    private void loadDashboardData() {
        try (Connection conn = DataBaseConnection.getConnection()) {
            loadArtistCount(conn);
            loadArtworkCount(conn);
            loadCustomerCount(conn);
            loadEventCount(conn);
            loadinqueriesCount(conn);
            loadRevenue(conn);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private void loadArtworkCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(painting_id) as Total_Paint FROM paintings";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Total_Paint");
                    artworkCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadCustomerCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(user_id) as Customer FROM users where role ='customer'";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Customer");
                    customerCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadEventCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(event_id) as Event FROM events ";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Event");
                    eventCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadinqueriesCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(contact_id) as q FROM contactus";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("q");
                    inqueriesCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadRevenue(Connection conn) throws SQLException {
        String query = "SELECT SUM(o.price) as Revenue FROM orderitems as o JOIN paintings as p ON o.painting_id = p.painting_id";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Revenue");
                    totalSellCount.setText(String.valueOf(count));
                }
            }
        }
    }

    private void loadArtistCount(Connection conn) throws SQLException {
        String query = "SELECT COUNT(user_id) as Artist FROM users where role ='artist'";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("Artist");
                    artistCount.setText(String.valueOf(count));
                }
            }
        }
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
        System.out.println("AdminDashboardController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("AdminDashboardController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("AdminDashboardController: Warning - controller is null");
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
