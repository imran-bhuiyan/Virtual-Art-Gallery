package com.example.test_project;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.sql.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ArtistMessageController extends BaseController {

    @FXML private ListView<String> customerListView;
    @FXML private ListView<String> messageListView;
    @FXML private TextField messageField;
    @FXML private Label selectedCustomerLabel;

    private Map<String, Integer> customerIdMap = new HashMap<>();
    private Map<Integer, String> onlineCustomers = new HashMap<>();
    private PrintWriter out;
    private BufferedReader in;

    public void initialize() {
        System.out.println("Initializing ArtistMessageController");
        setupCustomerListView();
        setupMessageListView();
    }

    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        System.out.println("Set userId to: " + userId);
        loadCustomers();
        connectToServer();
    }

    private void setupCustomerListView() {
        customerListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    int customerId = customerIdMap.get(item);
                    if (onlineCustomers.containsKey(customerId)) {
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        customerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedCustomerId = customerIdMap.get(newValue);
                openChatWithCustomer(selectedCustomerId, newValue);
            }
        });
    }

    private void setupMessageListView() {
        messageListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(this::readMessagesFromServer).start();
            System.out.println("Sending artist ID to server: " + userId);
            out.println(userId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessagesFromServer() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                System.out.println("Received message from server: " + message);
                if (message.startsWith("MESSAGE ")) {
                    String[] parts = message.split(" ", 3);
                    int senderId = Integer.parseInt(parts[1]);
                    String content = parts[2];
                    Platform.runLater(() -> displayMessage(senderId, content));
                } else if (message.startsWith("STATUS ")) {
                    String[] parts = message.split(" ");
                    int userId = Integer.parseInt(parts[1]);
                    boolean isOnline = parts[2].equals("ONLINE");
                    Platform.runLater(() -> updateCustomerStatus(userId, isOnline));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCustomers() {
        System.out.println("Loading customers for artist ID: " + userId);
        ObservableList<String> customers = FXCollections.observableArrayList();
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT DISTINCT u.user_id, u.name FROM Users u " +
                             "JOIN ChatMessages cm ON (u.user_id = cm.sender_id OR u.user_id = cm.receiver_id) " +
                             "WHERE (cm.sender_id = ? OR cm.receiver_id = ?) AND u.role = 'customer'")) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int customerId = rs.getInt("user_id");
                String customerName = rs.getString("name");
                customers.add(customerName);
                customerIdMap.put(customerName, customerId);
                System.out.println("Added customer: " + customerName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> customerListView.setItems(customers));
    }

    private void openChatWithCustomer(int customerId, String customerName) {
        System.out.println("Opening chat with customer: " + customerId + " - " + customerName);
        messageListView.getItems().clear();
        loadChatHistory(customerId);
        Platform.runLater(() -> selectedCustomerLabel.setText("Chat with: " + customerName));
    }

    private void loadChatHistory(int customerId) {
        System.out.println("Loading chat history for customer ID: " + customerId);
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT sender_id, content FROM ChatMessages " +
                             "WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                             "ORDER BY sent_at")) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, customerId);
            pstmt.setInt(3, customerId);
            pstmt.setInt(4, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int senderId = rs.getInt("sender_id");
                String content = rs.getString("content");
                displayMessage(senderId, content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(int senderId, String content) {
        Platform.runLater(() -> {
            String senderName = getSenderName(senderId);
            String message = senderName + ": " + content;
            System.out.println("Displaying message: " + message);
            messageListView.getItems().add(message);
        });
    }

    private String getSenderName(int senderId) {
        if (senderId == userId) {
            return "You";
        }
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT name FROM Users WHERE user_id = ?")) {
            pstmt.setInt(1, senderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Unable to find name for senderId: " + senderId);
        return "Unknown";
    }

    private void updateCustomerStatus(int customerId, boolean isOnline) {
        System.out.println("Updating customer status: " + customerId + " - " + (isOnline ? "Online" : "Offline"));
        if (isOnline) {
            onlineCustomers.put(customerId, "Online");
        } else {
            onlineCustomers.remove(customerId);
        }
        customerListView.refresh();
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty() && !customerIdMap.isEmpty()) {
            String selectedCustomerName = customerListView.getSelectionModel().getSelectedItem();
            if (selectedCustomerName != null) {
                int selectedCustomerId = customerIdMap.get(selectedCustomerName);
                System.out.println("Sending message to server: SEND " + selectedCustomerId + " " + message);
                out.println("SEND " + selectedCustomerId + " " + message);
                messageField.clear();
            }
        }
    }

    // Navigation methods

    @FXML
    void artistAddPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddPaint.fxml");
    }

    @FXML
    void artistMessages(ActionEvent event) throws IOException {
        // We're already on the messages page, so no action needed
    }

    @FXML
    void artistDashboard(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistDashboard.fxml");
    }

    @FXML
    void artistPaintings(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistPaintingPage.fxml");
    }

    @FXML
    void artistMyPainting(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistMyPaintPage.fxml");
    }

    @FXML
    void artistAddAuctions(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddAuction.fxml");
    }

    @FXML
    void artistMyProfile(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistProfile.fxml");
    }

    @FXML
    void artistLogout(ActionEvent event) throws IOException {
        loadPage(event, "Guest/userOrGuestHomePage.fxml");
    }

    @FXML
    void artistNotification(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistNotification.fxml");
    }

    @FXML
    void artistSeeAuction(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistSeeAuctionPage.fxml");
    }

    @FXML
    void artistOrders(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistOrderPage.fxml");
    }

    @FXML
    void artistAddNFTs(ActionEvent event) throws IOException {
        loadPage(event, "Artist/ArtistAddNFTs.fxml");
    }

    @FXML
    void artistSeeBalance(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Artist/ArtistBalanceWindow.fxml"));
            Parent root = loader.load();

            ArtistBalanceController balanceController = loader.getController();
            balanceController.setUserId(this.userId);
            balanceController.loadBalance();

            Stage balanceStage = new Stage();
            balanceStage.setTitle("Artist Balance");
            balanceStage.setScene(new Scene(root));
            balanceStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error message to the user)
        }
    }
}