package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class ArtistProfileController extends BaseController {
    @FXML
    private TextField name;
//    @FXML
//    private TextField id;
    @FXML
    private TextField email;
    @FXML
    private TextField dateOfbirth;
    @FXML
    private Button editProfileButton;

    private int userId;
    private boolean isEditing = false;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$"; // Simple email regex pattern


    @Override
    public void setUserId(int userId) {
        this.userId = userId;
        loadUserData();
    }

    private void loadUserData() {
        try (Connection conn = DataBaseConnection.getConnection()) {
            String query = "SELECT name, email, dob FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        name.setText(rs.getString("name"));
//                        id.setText(String.valueOf(userId));
                        email.setText(rs.getString("email"));
                        dateOfbirth.setText(rs.getString("dob"));

                        setFieldsEditable(false);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }

    private void setFieldsEditable(boolean editable) {
        name.setEditable(editable);
//        id.setEditable(false);
        email.setEditable(editable);
        dateOfbirth.setEditable(false);
    }

    @FXML
    void editProfile(ActionEvent event) {
        if (!isEditing) {
            // Enable editing
            setFieldsEditable(true);
            editProfileButton.setText("Save Changes");
            isEditing = true;
        } else {
            // Validate and save changes
            if (validateEmail() && checkEmailUniqueness()) {
                if (saveChanges()) {
                    setFieldsEditable(false);
                    editProfileButton.setText("Edit Profile");
                    isEditing = false;
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully.");
                }
            }
        }
    }

    private boolean validateEmail() {
        String emailText = email.getText();
        if (!Pattern.matches(EMAIL_REGEX, emailText)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
            return false;
        }
        return true;
    }

    private boolean checkEmailUniqueness() {
        String emailText = email.getText();
        try (Connection conn = DataBaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id != ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, emailText);
                pstmt.setInt(2, userId);  // Exclude the current user

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert(Alert.AlertType.ERROR, "Email Already Taken", "The email is already associated with another account.");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error checking email uniqueness.");
            return false;
        }
        return true;
    }




    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean saveChanges() {
        try (Connection conn = DataBaseConnection.getConnection()) {
            String query = "UPDATE users SET name = ?, email = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name.getText());
                pstmt.setString(2, email.getText());
                pstmt.setInt(3, userId);

                int rowsUpdated = pstmt.executeUpdate();
                return rowsUpdated > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    void changePassword(ActionEvent event) throws IOException {
        // Load a new window for password change
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Artist/ArtistPasswordChange.fxml"));
        Parent root = loader.load();

        // Set the user ID in the controller of the new window
        BaseController controller = loader.getController();
        controller.setUserId(userId);

        Stage stage = new Stage();
        stage.setTitle("Change Password");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL); // Block interaction with other windows
        stage.showAndWait();
    }

    // Navigation methods (same as in ArtistAddPaintController)
    @FXML
    void artistAddAuctions(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddAuction.fxml");
    }

    @FXML
    void artistAddNFTs(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddNFTs.fxml");
    }

    @FXML
    void artistDashboard(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistDashboard.fxml");
    }

    @FXML
    void artistLogout(ActionEvent event) throws IOException {
        System.out.println("ArtistAddNFTController: Logging out user");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Customer/CustomerLoginPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
        System.out.println("ArtistAddNFTController: Navigated to login page");
    }

    @FXML
    void artistMessages(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistMessage.fxml");
    }

    @FXML
    void artistMyPainting(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistMyPaintPage.fxml");
    }

    @FXML
    void artistMyProfile(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistProfile.fxml");
    }

    @FXML
    void artistNotification(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNotification.fxml");
    }

    @FXML
    void artistPaintings(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistPaintingPage.fxml");
    }

    @FXML
    void artistSeeAuction(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistSeeAuctionPage.fxml");
    }

    @FXML
    void artistOrders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistOrderPage.fxml");
    }


    @FXML
    void artistAddPainting(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistAddPaint.fxml");
    }

    @FXML
    void mynft(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNFTPage.fxml");
    }


    @FXML
    void nftorders(ActionEvent event) throws IOException {
        loadPageWithUserId(event, "Artist/ArtistNFTorders.fxml");

    }

    @FXML
    void withdrawcash(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Artist/ArtistWithdrawMoney.fxml"));
            Parent root = loader.load();

            ArtistWithdrawMoneyController balanceController = loader.getController();
            balanceController.setUserId(this.userId);


            Stage balanceStage = new Stage();
            balanceStage.setTitle("Artist Withdraw");
            balanceStage.setScene(new Scene(root));
            balanceStage.show();
        } catch (IOException e) {
            e.printStackTrace();

        }


    }
    @FXML
    void withdrawHistory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Artist/ArtistWithdrawHistory.fxml"));
            Parent root = loader.load();

            ArtistWithdrawHistoryController historyController = loader.getController();
            historyController.setUserId(this.userId);

            Stage historyStage = new Stage();
            historyStage.setTitle("Withdraw History");
            historyStage.setScene(new Scene(root));
            historyStage.initModality(Modality.APPLICATION_MODAL);
            historyStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
        System.out.println("ArtistAddNFTController: loadPageWithUserId() called with path: " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        if (controller != null) {
            System.out.println("ArtistAddNFTController: Setting userId " + userId + " on new controller");
            controller.setUserId(this.userId);
        } else {
            System.out.println("ArtistAddNFTController: Warning - controller is null");
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