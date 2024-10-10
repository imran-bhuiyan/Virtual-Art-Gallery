package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class adminEditArtistController extends BaseController {

    @FXML
    private TextField name;

    @FXML
    private TextField email;

    @FXML
    private DatePicker birth;

    private int artistId;



    public void setArtistId(int artistId) {
        this.artistId = artistId;
        loadArtistDetails();
    }

    private void loadArtistDetails() {
        String query = "SELECT name, email, dob FROM users WHERE user_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, artistId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    name.setText(rs.getString("name"));
                    email.setText(rs.getString("email"));
                    birth.setValue(rs.getDate("dob").toLocalDate());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load artist details.");
        }
    }

    @FXML
    void addTodatabase(ActionEvent event) {
        String fullName = name.getText();
        String emailAddress = email.getText();
        LocalDate birthDate = birth.getValue();

        if (fullName.isEmpty() || emailAddress.isEmpty() || birthDate == null) {
            showAlert(Alert.AlertType.ERROR, "Update Error", "Please fill in all fields.");
            return;
        }


        if (!isValidEmail(emailAddress)) {
            showAlert(Alert.AlertType.ERROR, "Update Error", "Please enter a valid email address.");
            return;
        }
        if (!isEmailUnique(emailAddress)) {
            showAlert(Alert.AlertType.ERROR, "Update Error", "Email already exists. Please use a different email.");
            return;
        }

        String query = "UPDATE users SET name = ?, email = ?, dob = ? WHERE user_id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, emailAddress);
            pstmt.setDate(3, java.sql.Date.valueOf(birthDate));
            pstmt.setInt(4, artistId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Artist updated successfully.");
                ((Stage) name.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update artist.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating.");
        }
    }

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




//    @FXML
//    void deleteArtist(ActionEvent event) {
//        String query = "DELETE FROM users WHERE user_id = ?";
//        try (Connection conn = DataBaseConnection.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(query)) {
//
//            pstmt.setInt(1, artistId);
//
//            int affectedRows = pstmt.executeUpdate();
//            if (affectedRows > 0) {
//                showAlert(Alert.AlertType.INFORMATION, "Success", "Artist deleted successfully.");
//                ((Stage) name.getScene().getWindow()).close();
//            } else {
//                showAlert(Alert.AlertType.ERROR, "Delete Error", "Failed to delete artist.");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while deleting.");
//        }
//    }

    @FXML
    void clear(ActionEvent event) {
        name.clear();
        email.clear();
        birth.setValue(null);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }



//    @FXML
//    void backTomanageArtist(ActionEvent event) throws IOException {
//        loadPageWithUserId(event, "Admin/AdminManageArtist.fxml");
//
//
//    }
//    private void loadPageWithUserId(ActionEvent event, String fxmlPath) throws IOException {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
//        Parent root = loader.load();
//
//        BaseController controller = loader.getController();
//        if (controller != null) {
//            controller.setUserId(this.userId);
//        }
//
//        Scene scene = new Scene(root);
//        Stage stage;
//
//        if (event.getSource() instanceof MenuItem) {
//            MenuItem menuItem = (MenuItem) event.getSource();
//            stage = (Stage) menuItem.getParentPopup().getOwnerWindow();
//        } else if (event.getSource() instanceof Node) {
//            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        } else {
//            throw new IllegalArgumentException("Event source not recognized");
//        }
//
//        stage.setScene(scene);
//        stage.show();
//    }
}


