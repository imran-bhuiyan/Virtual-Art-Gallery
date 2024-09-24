package com.example.test_project;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 5000;
    private static Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket);

            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String name;
    private int userId; // Assuming each user has a unique ID

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            name = in.readLine();
            userId = getUserIdFromDatabase(name); // You need to implement this method
            ChatServer.broadcastMessage(name + " has joined the chat", this);

            String message;
            while ((message = in.readLine()) != null) {
                ChatServer.broadcastMessage(name + ": " + message, this);
                saveMessageToDatabase(userId, -1, message); // -1 for broadcast messages
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ChatServer.removeClient(this);
                ChatServer.broadcastMessage(name + " has left the chat", this);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void sendMessage(String message) {
        out.println(message);
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
        String sql = "SELECT user_id FROM users WHERE username = ?";

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
}