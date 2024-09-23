package com.example.test_project;

import com.almasb.fxgl.entity.action.Action;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdminMessagesController {

    @FXML
    private ListView<String> userListView;

    @FXML
    private VBox chatBox;

    @FXML
    private TextField messageField;

    private Map<String, ObservableList<String>> userChats = new HashMap<>();
    private String currentUser = null;

    @FXML
    public void initialize() {
        // Add some sample users
        ObservableList<String> users = FXCollections.observableArrayList("User 1", "User 2", "User 3");
        userListView.setItems(users);

        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentUser = newValue;
                displayChat(currentUser);
            }
        });

        // Select the first user by default
        userListView.getSelectionModel().selectFirst();
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
            return;
        }

        String message = messageField.getText().trim();
        ObservableList<String> chat = userChats.get(currentUser);
        chat.add("admin: " + message);
        addMessageToChat("admin: " + message, true);
        messageField.clear();
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
    private void goBack(ActionEvent event)throws IOException {
        System.out.println("Navigating back to admin dashboard");
            loadPage(event,"Admin/AdminDashboard.fxml");
    }
    private void loadPage(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
