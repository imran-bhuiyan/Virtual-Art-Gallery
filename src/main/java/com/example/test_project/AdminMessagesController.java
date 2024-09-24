package com.example.test_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AdminMessagesController {

    @FXML
    private ListView<String> userListView;

    @FXML
    private VBox chatBox;

    @FXML
    private TextField messageField;

    @FXML
    private TextField userSearchField;

    private Map<String, ObservableList<String>> userChats = new HashMap<>();
    private ObservableList<String> users = FXCollections.observableArrayList();
    private String currentUser = null;

    @FXML
    public void initialize() {
        userListView.setItems(users);

        // Load initial users from the database
        loadAllUsers();

        // Add listener to search field
        userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                loadAllUsers(); // Reload all users if search is empty
            } else {
                searchUsers(newValue);
            }
        });

        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentUser = newValue;
                displayChat(currentUser);
            }
        });

        // Select the first user by default if available
        if (!users.isEmpty()) {
            userListView.getSelectionModel().selectFirst();
        }
    }

    private void loadAllUsers() {
        users.clear(); // Clear current list

        try (Connection connection = DataBaseConnection.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT name FROM users");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchUsers(String query) {
        users.clear(); // Clear current list

        try (Connection connection = DataBaseConnection.getConnection()) {
            String sql = "SELECT name FROM users WHERE name LIKE ? OR email LIKE ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, "%" + query + "%");
            statement.setString(2, "%" + query + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                users.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayChat(String user) {
        chatBox.getChildren().clear();
        ObservableList<String> chat = userChats.computeIfAbsent(user, k -> FXCollections.observableArrayList());

        for (String message : chat) {
            addMessageToChat(message, false);
        }
    }

    @FXML
    private void sendMessage() {
        if (currentUser == null || messageField.getText().trim().isEmpty()) {
            return; // Do nothing if no current user or empty message
        }

        String message = messageField.getText().trim();

        // Store the message in the user's chat history
        ObservableList<String> chat = userChats.computeIfAbsent(currentUser, k -> FXCollections.observableArrayList());
        chat.add("admin: " + message);

        addMessageToChat("admin: " + message, true);

        messageField.clear(); // Clear input field after sending
    }

    private void addMessageToChat(String message, boolean isAdmin) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(isAdmin ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Text text = new Text(message);
        text.setWrappingWidth(500);

        messageBox.getChildren().add(text);
        chatBox.getChildren().add(messageBox);
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        System.out.println("Navigating back to admin dashboard");
        loadPage(event, "Admin/AdminDashboard.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}