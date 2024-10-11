package com.example.test_project;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;


public class ChatServer {
    private static final int PORT = 12345;
    private static final Map<Integer, PrintWriter> clientWriters = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Chat Server is running on port " + PORT);
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    private static class Handler implements Runnable {
        private Socket socket;
        private int userId;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                userId = authenticateUser(in, out);
                if (userId == -1) {
                    System.out.println("Authentication failed for a client");
                    return;
                }

                System.out.println("User " + userId + " connected");
                clientWriters.put(userId, out);
                broadcastOnlineStatus(userId, true);

                sendPreviousMessages(userId, out);

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("SEND ")) {
                        String[] parts = input.split(" ", 3);
                        int receiverId = Integer.parseInt(parts[1]);
                        String message = parts[2];
                        broadcastMessage(userId, receiverId, message);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error handling client " + userId + ": " + e);
            } finally {
                if (userId != -1) {
                    System.out.println("User " + userId + " disconnected");
                    clientWriters.remove(userId);
                    broadcastOnlineStatus(userId, false);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket for user " + userId + ": " + e);
                }
            }
        }

        private void broadcastOnlineStatus(int userId, boolean isOnline) {
            String status = isOnline ? "ONLINE" : "OFFLINE";
            for (Map.Entry<Integer, PrintWriter> entry : clientWriters.entrySet()) {
                if (entry.getKey() != userId) {
                    entry.getValue().println("STATUS " + userId + " " + status);
                }
            }
            System.out.println("Broadcasted " + status + " status for user " + userId);
        }

        private int authenticateUser(BufferedReader in, PrintWriter out) throws IOException {
            out.println("SUBMIT_ID");
            int id = Integer.parseInt(in.readLine());
            // TODO: Implement proper authentication here
            return id;
        }

        private void sendPreviousMessages(int userId, PrintWriter out) {
            try (Connection conn = DataBaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT sender_id, content, sent_at FROM ChatMessages WHERE receiver_id = ? ORDER BY sent_at")) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    out.println("MESSAGE " + rs.getInt("sender_id") + " " + rs.getString("content"));
                }
                System.out.println("Sent previous messages to user " + userId);
            } catch (SQLException e) {
                System.out.println("Error sending previous messages to user " + userId + ": " + e);
            }
        }

        private void broadcastMessage(int senderId, int receiverId, String message) {
            PrintWriter senderWriter = clientWriters.get(senderId);
            PrintWriter receiverWriter = clientWriters.get(receiverId);

            if (senderWriter != null) {
                senderWriter.println("MESSAGE " + senderId + " " + message);
            }
            if (receiverWriter != null) {
                receiverWriter.println("MESSAGE " + senderId + " " + message);
            }

            saveMessageToDatabase(senderId, receiverId, message);
            System.out.println("Broadcasted message from user " + senderId + " to user " + receiverId);
        }

        private void saveMessageToDatabase(int senderId, int receiverId, String message) {
            try (Connection conn = DataBaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO ChatMessages (sender_id, receiver_id, content) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, senderId);
                pstmt.setInt(2, receiverId);
                pstmt.setString(3, message);
                pstmt.executeUpdate();
                System.out.println("Saved message from user " + senderId + " to user " + receiverId + " in database");
            } catch (SQLException e) {
                System.out.println("Error saving message to database: " + e);
            }
        }
    }
}