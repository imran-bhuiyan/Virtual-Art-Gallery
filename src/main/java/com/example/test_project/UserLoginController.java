package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserLoginController {

    @FXML
    private PasswordField userPass;

    @FXML
    private TextField username;

    private String userRole;

    @FXML
    void goCustomerHome(ActionEvent event) throws IOException {
        String uname = username.getText();
        String pass = userPass.getText();

        if (validateLogin(uname, pass)) {
            if ("customer".equals(userRole)) {
                loadPage(event, "Customer/customerHomePage.fxml");
            } else if ("artist".equals(userRole)) {
                 loadPage(event, "Artist/ArtistHomePage.fxml");
            } else if ("admin".equals(userRole)) {
                loadPage(event, "Admin/AdminDashboard.fxml");
            }
        } else {
            showAlert("Login Failed", "Incorrect username or password.");
        }
    }

    private boolean validateLogin(String username, String password) {
        boolean isValid = false;
        String query = "SELECT password, role FROM Users WHERE name = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    userRole = rs.getString("role");

                    if (storedPassword.equals(password)) {
                        isValid = true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isValid;
    }

    @FXML
    void goHome(ActionEvent event) throws IOException {
        loadPage(event, "guest/UserOrGuestHomePage.fxml");
    }

    @FXML
    void goGuestRegistrationForm(ActionEvent event) throws IOException {
        loadPage(event, "guest/guestRegistrationForm.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        System.out.println(username);

//        if(username != null) {
//            ArtistPageController ar=loader.getController();
//            ar.setWelcomeText(username);
//        }

//        if (username != null) {
//            Object controller = loader.getController();
//            if (controller instanceof ArtistPageController) {
//                ArtistPageController artistController = (ArtistPageController) controller;
//                artistController.setWelcomeText(username);
//            }
//        }





            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }

        private void showAlert (String title, String message){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }