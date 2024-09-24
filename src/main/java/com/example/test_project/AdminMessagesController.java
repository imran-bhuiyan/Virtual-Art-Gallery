package com.example.test_project;

import javafx.application.Platform;
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
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminMessagesController {

    @FXML
    private ListView<String> userListView;

    @FXML
    private VBox chatBox;

    @FXML
    private TextField messageField;

    @FXML
    private TextField userSearchField;

    @FXML
    private Text currentUserName;

    private Map<String, ObservableList<String>> userChats = new HashMap<>();
    private ObservableList<String> users = FXCollections.observableArrayList();
    private String currentUser = null;
    private ExecutorService executorService;
    private ChatClient chatClient;
    private int adminId;

    @FXML
    public void initialize() {
        executorService = Executors.newFixedThreadPool(2);
        chatClient = new ChatClient();

        userListView.setItems(users);
        loadAllUsers();

        userSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            executorService.submit(() -> searchUsers(newValue));
        });

        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentUser = newValue;
                currentUserName.setText(currentUser);
                displayChat(currentUser);
            }
        });

        if (!users.isEmpty()) {
            userListView.getSelectionModel().selectFirst();
        }

        startMessageListener();

        // Get admin ID
        adminId = getAdminId();
    }

    private void loadAllUsers() {
        executorService.submit(() -> {
            try (Connection connection = DataBaseConnection.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT name FROM users WHERE role != 'admin'");
                ResultSet resultSet = statement.executeQuery();
                ObservableList<String> loadedUsers = FXCollections.observableArrayList();
                while (resultSet.next()) {
                    loadedUsers.add(resultSet.getString("name"));
                }
                Platform.runLater(() -> users.setAll(loadedUsers));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void searchUsers(String query) {
        executorService.submit(() -> {
            try (Connection connection = DataBaseConnection.getConnection()) {
                String sql = "SELECT name FROM users WHERE (name LIKE ? OR email LIKE ?) AND role != 'admin'";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, "%" + query + "%");
                statement.setString(2, "%" + query + "%");
                ResultSet resultSet = statement.executeQuery();

                ObservableList<String> searchResults = FXCollections.observableArrayList();
                while (resultSet.next()) {
                    searchResults.add(resultSet.getString("name"));
                }
                Platform.runLater(() -> users.setAll(searchResults));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void displayChat(String user) {
        Platform.runLater(() -> {
            chatBox.getChildren().clear();
            ObservableList<String> chat = userChats.computeIfAbsent(user, k -> FXCollections.observableArrayList());

            // Load chat history from database
            loadChatHistory(user);

            for (String message : chat) {
                addMessageToChat(message, message.startsWith("admin:"));
            }
        });
    }

    private void loadChatHistory(String username) {
        int userId = getUserIdFromDatabase(username);
        if (userId == -1) return;

        String sql = "SELECT sender_id, content FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY sent_at ASC";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, adminId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            pstmt.setInt(4, adminId);

            ResultSet rs = pstmt.executeQuery();

            ObservableList<String> chat = userChats.get(username);
            while (rs.next()) {
                int senderId = rs.getInt("sender_id");
                String content = rs.getString("content");
                String message = (senderId == adminId ? "admin: " : username + ": ") + content;
                chat.add(message);
            }
        } catch (SQLException e) {
            System.out.println("Error loading chat history: " + e.getMessage());
        }
    }

    @FXML
    private void sendMessage() {
        if (currentUser == null || messageField.getText().trim().isEmpty()) {
            return;
        }

        String message = messageField.getText().trim();
        String finalMessage = "admin: " + message;

        executorService.submit(() -> {
            int receiverId = getUserIdFromDatabase(currentUser);
            if (receiverId != -1) {
                saveMessageToDatabase(adminId, receiverId, message);
                chatClient.sendMessage(finalMessage);
                Platform.runLater(() -> {
                    ObservableList<String> chat = userChats.computeIfAbsent(currentUser, k -> FXCollections.observableArrayList());
                    chat.add(finalMessage);
                    addMessageToChat(finalMessage, true);
                    messageField.clear();
                });
            } else {
                Platform.runLater(() -> {
                    // Handle error - user not found
                    System.out.println("Error: User not found in database");
                });
            }
        });
    }

    private void saveMessageToDatabase(int senderId, int receiverId, String content) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, sent_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setString(3, content);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving message to database: " + e.getMessage());
        }
    }

    private int getUserIdFromDatabase(String username) {
        String sql = "SELECT user_id FROM users WHERE name = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.out.println("Error getting user ID from database: " + e.getMessage());
        }

        return -1; // Return -1 if user not found
    }

    private int getAdminId() {
        String sql = "SELECT user_id FROM users WHERE role = 'admin' LIMIT 1";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.out.println("Error getting admin ID from database: " + e.getMessage());
        }

        return -1; // Return -1 if admin not found
    }

    private void addMessageToChat(String message, boolean isAdmin) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(isAdmin ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Text text = new Text(message);
        text.setWrappingWidth(500);

        messageBox.getChildren().add(text);
        chatBox.getChildren().add(messageBox);
    }

    private void startMessageListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (true) {
                    String message = chatClient.receiveMessage();
                    Platform.runLater(() -> {
                        ObservableList<String> chat = userChats.computeIfAbsent(currentUser, k -> FXCollections.observableArrayList());
                        chat.add(message);
                        addMessageToChat(message, false);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
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

    public void shutdown() {
        executorService.shutdown();
    }
}