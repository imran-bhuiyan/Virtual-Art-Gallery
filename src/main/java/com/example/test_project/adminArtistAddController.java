package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javafx.stage.Stage;

import java.io.IOException;

public class adminArtistAddController extends BaseController {


    @FXML
    private DatePicker birthday;

    @FXML
    private TextField email;

    @FXML
    private TextField name;

    @FXML
    private PasswordField password;

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("x: setUserId() called with userId: " + userId);

    }

    @FXML
    void addTodatabase(ActionEvent event) {
        String fullName = name.getText();
        String emailAddress = email.getText();
        String passwordText = password.getText();
        LocalDate birthDate = birthday.getValue();

        if (fullName.isEmpty() || emailAddress.isEmpty() || passwordText.isEmpty() || birthDate == null) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields.");
            return;
        }
        if (!isValidEmail(emailAddress)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please enter a valid email address.");
            return;
        }
        if (!isEmailUnique(emailAddress)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Email already exists. Please use a different email.");
            return;
        }

        try {
            addArtist(fullName, emailAddress, passwordText, birthDate);
        }
        catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while registering.");        }
    }

    private void addArtist(String fullName, String email, String password, LocalDate birthDate) throws SQLException {

        String query = "INSERT INTO users (name, email, password, dob, role) VALUES (?, ?, ?, ?, 'artist')";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setDate(4, java.sql.Date.valueOf(birthDate));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlertSuccess(Alert.AlertType.INFORMATION, "Success", "Successfully Registered.");
                clear(null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Registration Error", "Email already exists. Please use a different email.");
            }
        }
    }

//    email address validation

    private boolean isValidEmail(String emailAddress) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return emailAddress.matches(emailRegex);
    }

    private boolean isEmailUnique(String emailAddress) {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?")) {

            pstmt.setString(1, emailAddress);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    @FXML
    void clear(ActionEvent event) {
        name.clear();
        email.clear();
        password.clear();
        birthday.setValue(null);
    }

    private void showAlert(Alert.AlertType alertType,String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Success Alert
    private void showAlertSuccess(Alert.AlertType alertType,String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();

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