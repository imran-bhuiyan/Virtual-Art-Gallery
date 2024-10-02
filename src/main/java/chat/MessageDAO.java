package chat;

import com.example.test_project.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Method to save a message to the database
    public static void saveMessage(int senderId, int receiverId, String content, String timestamp) {
        String query = "INSERT INTO messages (sender_id, receiver_id, content, sent_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);
            stmt.setString(4, timestamp); // Assuming timestamp is already in String format
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to get message history between two users
    public static List<Data> getMessageHistory(int senderId, int receiverId) {
        List<Data> history = new ArrayList<>();
        String query = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY sent_at";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, receiverId);
            stmt.setInt(4, senderId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String message = rs.getString("content");
                    String sentAt = rs.getString("sent_at");

                    // Debugging: Print out sentAt to ensure correct format
                    System.out.println("Sent at: " + sentAt);

                    // Convert sentAt to LocalDateTime
                    LocalDateTime timestamp = LocalDateTime.parse(sentAt, formatter);

                    // Store the message with the parsed LocalDateTime
                    history.add(new Data(message, String.valueOf(senderId), String.valueOf(receiverId), timestamp)); // Adjust if Data class requires int
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // Method to get a list of users with whom the logged-in user has chatted
    public static List<String> getChatUsers(int loggedInUser) {
        List<String> chatUsers = new ArrayList<>();
        String query = "SELECT DISTINCT CASE WHEN sender_id = ? THEN receiver_id ELSE sender_id END AS chat_user " +
                "FROM messages WHERE sender_id = ? OR receiver_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, loggedInUser);
            stmt.setInt(2, loggedInUser);
            stmt.setInt(3, loggedInUser);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chatUsers.add(rs.getString("chat_user"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chatUsers;
    }
}
