package com.example.test_project;

import chat.Data;
import chat.MessageDAO;
import chat.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class AdminMessagesController {

    @FXML
    private ListView<String> userListView;
    @FXML
    private VBox chatBox;
    @FXML
    private TextField messageField;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField searchField;

    private String currentUser; // This can remain a String if it holds the username
    private int loggedInUser; // Change this to int

    @FXML
    public void initialize() {
        loggedInUser = 1;
        loadChatList();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchUsers(newValue);
        });

        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentUser = newValue; // Assuming currentUser is a username
                loadMessageHistory(currentUser); // Pass username to loadMessageHistory
            }
        });
    }

    private void loadChatList() {
        List<String> chatUsers = MessageDAO.getChatUsers(loggedInUser);
        userListView.getItems().clear();
        userListView.getItems().addAll(chatUsers);
    }

    private void searchUsers(String searchTerm) {
        List<String> users = UserDAO.searchUsers(searchTerm); // Ensure searchUsers method is implemented in UserDAO
        userListView.getItems().clear();
        userListView.getItems().addAll(users);
    }

    private void loadMessageHistory(String selectedUser) {
        chatBox.getChildren().clear();

        // Assuming you have a method in UserDAO to get user ID from username
        Integer selectedUserId = UserDAO.getUserIdFromUsername(selectedUser); // Create this method to get user ID
        if (selectedUserId != null) {
            List<Data> history = MessageDAO.getMessageHistory(loggedInUser, selectedUserId); // Pass as int

            for (Data data : history) {
                Label messageLabel = new Label(data.getTimestamp() + " " + data.getSenderId() + ": " + data.getMessage());
                chatBox.getChildren().add(messageLabel);
            }
        }

        scrollPane.setVvalue(1.0);
    }

    @FXML
    public void goBack(ActionEvent event) throws IOException {
        loadPage(event, "Admin/AdminDashboard.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty() && currentUser != null) {
            Data data = new Data(message, String.valueOf(loggedInUser), currentUser, LocalDateTime.now());

            // Call saveMessage with the correct parameters
            MessageDAO.saveMessage(loggedInUser, Integer.parseInt(currentUser), message, data.getFormattedTimestamp());

            Label messageLabel = new Label("You: " + message);
            chatBox.getChildren().add(messageLabel);

            messageField.clear();
            scrollPane.setVvalue(1.0);

            // Load the message history again to ensure it's updated
            loadMessageHistory(currentUser);
        }
    }
}
