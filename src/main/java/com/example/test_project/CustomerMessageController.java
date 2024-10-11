// CustomerMessageController.java
package com.example.test_project;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.sql.*;

public class CustomerMessageController extends BaseController {

    @FXML private ListView<String> messageListView;
    @FXML private TextField messageField;
    @FXML private Label chatPartnerLabel;
    @FXML private ListView<String> chatPartnerListView;

    private PrintWriter out;
    private BufferedReader in;
    private int currentArtistId;
    private String currentArtistName;
    private boolean isHandlingSelection = false;

    @FXML
    public void initialize() {
        System.out.println("Initializing CustomerMessengerController");

        validateComponents();

        loadChatPartners();
    }

    private void validateComponents() {
        if (messageListView == null) System.err.println("Error: messageListView is null");
        if (messageField == null) System.err.println("Error: messageField is null");
        if (chatPartnerLabel == null) System.err.println("Error: chatPartnerLabel is null");
        if (chatPartnerListView == null) System.err.println("Error: chatPartnerListView is null");
    }

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("CustomerMessengerController: Setting userId to " + userId);
        connectToServer();
        loadChatPartners();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(this::readMessagesFromServer).start();

            out.println(userId);
            System.out.println("Connected to server successfully");
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
            showAlert("Connection Error", "Failed to connect to the server. Please try again later.");
        }
    }

    private void readMessagesFromServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received message: " + message);
                if (message.startsWith("MESSAGE ")) {
                    String[] parts = message.split(" ", 3);
                    int senderId = Integer.parseInt(parts[1]);
                    String content = parts[2];
                    Platform.runLater(() -> displayMessage(senderId, content));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading messages from server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayMessage(int senderId, String content) {
        String displayText = (senderId == userId) ? "You: " + content : "Artist: " + content;
        System.out.println("Displaying message: " + displayText);
        if (messageListView != null) {
            messageListView.getItems().add(displayText);
        } else {
            System.err.println("Error: messageListView is null when trying to display message");
        }
    }

    @FXML
    void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && currentArtistId != 0) {
            System.out.println("Sending message: " + message + " to artist ID: " + currentArtistId);
            out.println("SEND " + currentArtistId + " " + message);
//            saveChatMessage(userId, currentArtistId, message);
            messageField.clear();
        } else {
            System.out.println("Message not sent. Empty message or no artist selected.");
        }
    }

    private void saveChatMessage(int senderId, int receiverId, String content) {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO ChatMessages (sender_id, receiver_id, content, sent_at, is_read) VALUES (?, ?, ?, NOW(), false)")) {
            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setString(3, content);
            pstmt.executeUpdate();
            System.out.println("Message saved to database");
        } catch (SQLException e) {
            System.err.println("Error saving message to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void initChat(int artistId, String artistName) {
        System.out.println("Initializing chat with artist ID: " + artistId + ", name: " + artistName);

        // Check if artist exists in chat list
        boolean artistExists = checkArtistExistsInChatHistory(artistId);

        // If artist doesn't exist in chat list, add them
        if (!artistExists) {
            System.out.println("Adding new artist to chat list: " + artistName);
            addInitialChatMessage(artistId);
        }

        // Set current artist and update UI
        this.currentArtistId = artistId;
        this.currentArtistName = artistName;
        chatPartnerLabel.setText("Chat with " + artistName);

        // Reload chat list and history
        loadChatPartners();
        loadChatHistory(artistId);
        selectArtistInList(artistName);
    }

    private boolean checkArtistExistsInChatHistory(int artistId) {
        String query = "SELECT COUNT(*) FROM ChatMessages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, artistId);
            pstmt.setInt(3, artistId);
            pstmt.setInt(4, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addInitialChatMessage(int artistId) {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO ChatMessages (sender_id, receiver_id, content, sent_at, is_read) VALUES (?, ?, ?, NOW(), false)")) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, artistId);
            pstmt.setString(3, "Chat started");
            pstmt.executeUpdate();
            System.out.println("Initial chat message created for artist ID: " + artistId);
        } catch (SQLException e) {
            System.err.println("Error creating initial chat message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void selectArtistInList(String artistName) {
        Platform.runLater(() -> {
            chatPartnerListView.getSelectionModel().select(artistName);
        });
    }

    private void loadChatHistory(int artistId) {
        messageListView.getItems().clear();
        String query = "SELECT sender_id, content FROM ChatMessages " +
                "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                "ORDER BY sent_at";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, artistId);
            pstmt.setInt(3, artistId);
            pstmt.setInt(4, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (!rs.getString("content").equals("Chat started")) {
                    displayMessage(rs.getInt("sender_id"), rs.getString("content"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading chat history: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Failed to load chat history. Please try again later.");
        }
    }

    private void loadChatPartners() {
        System.out.println("Loading chat partners for user ID: " + userId);
        chatPartnerListView.getItems().clear();
        String query = "SELECT DISTINCT u.user_id, u.name " +
                "FROM Users u " +
                "JOIN ChatMessages m ON (u.user_id = m.sender_id OR u.user_id = m.receiver_id) " +
                "WHERE (m.sender_id = ? OR m.receiver_id = ?) AND u.user_id != ? AND u.role = 'artist' " +
                "ORDER BY (SELECT MAX(sent_at) FROM ChatMessages " +
                "WHERE (sender_id = ? AND receiver_id = u.user_id) OR " +
                "(sender_id = u.user_id AND receiver_id = ?)) DESC";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            pstmt.setInt(4, userId);
            pstmt.setInt(5, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String artistName = rs.getString("name");
                chatPartnerListView.getItems().add(artistName);
            }

            setupChatPartnerSelection();
        } catch (SQLException e) {
            System.err.println("Error loading chat partners: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Failed to load chat partners. Please try again later.");
        }
    }

    private void setupChatPartnerSelection() {
        chatPartnerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !isHandlingSelection) {
                try {
                    isHandlingSelection = true;

                    // Only load chat if a new artist is selected
                    if (currentArtistName != null && currentArtistName.equals(newValue)) {
                        return;
                    }

                    System.out.println("Selected chat partner: " + newValue);
                    int artistId = getArtistIdByName(newValue);
                    if (artistId != -1) {
                        currentArtistId = artistId;
                        currentArtistName = newValue;
                        chatPartnerLabel.setText("Chat with " + newValue);

                        // Clear previous messages and load new chat history
                        messageListView.getItems().clear();  // Clear messages when switching profiles
                        loadChatHistory(artistId);
                    } else {
                        System.err.println("Could not find artist ID for: " + newValue);
                        showAlert("Error", "Could not load chat with selected artist.");
                        if (oldValue != null) {
                            Platform.runLater(() -> chatPartnerListView.getSelectionModel().select(oldValue));
                        }
                    }
                } finally {
                    isHandlingSelection = false;
                }
            }
        });
    }

    private int getArtistIdByName(String artistName) {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT user_id FROM Users WHERE name = ? AND role = 'artist'")) {
            pstmt.setString(1, artistName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // Navigation methods
    @FXML void CustomerLogout(ActionEvent event) throws IOException {
        loadPage(event, "Guest/userOrGuestHomePage.fxml");
    }

    @FXML void goCustomerHome(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerHomePage.fxml");
    }

    @FXML void goCustomerPaintings(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerPaintingPage.fxml");
    }

    @FXML void goCustomerTopArt(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerPageTopArt.fxml");
    }

    @FXML void goCustomerEvents(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerEventPage.fxml");
    }

    @FXML void goCustomerAuction(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerLiveAuctionPage.fxml");
    }

    @FXML void goCustomerNFT(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerNFTpage.fxml");
    }

    @FXML void goCustomerProfile(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerProfilePage.fxml");
    }

    @FXML void goCustomerCart(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerPaintingCheckout.fxml");
    }

    @FXML void goCustomerNotification(ActionEvent event) throws IOException {
        loadPage(event, "Customer/customerNotification.fxml");
    }

    @FXML void goCustomerMessages(ActionEvent event) {
        System.out.println("Already on the messages page");
    }
}