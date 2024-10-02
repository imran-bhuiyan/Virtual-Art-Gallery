package chat;

import com.example.test_project.DataBaseConnection;

import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class UserHandler implements Runnable {
    private Socket socket;
    private NetworkConnection connection;
    private String username;

    public UserHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            connection = new NetworkConnection(socket);

            // Receive username from client
            username = (String) connection.read();
            Information userInfo = new Information(username, connection);
            ServerMain.addUser(userInfo);

            System.out.println(username + " has joined the chat.");

            while (true) {
                Object msg = connection.read();
                if (msg instanceof String) {
                    handleMessage((String) msg);
                }
            }
        } catch (IOException e) {
            System.out.println(username + " disconnected.");
            ServerMain.removeUser(new Information(username, connection)); // Remove user on disconnection
        }
    }

    private void handleMessage(String msg) {
        String[] parts = msg.split("\\$");
        String sender = parts[0];
        String receiver = parts[1];
        String command = parts[2];

        if (command.equals("send")) {
            String messageContent = parts[3];

            // Deliver message to the receiver
            Information receiverInfo = ServerMain.getUserByUsername(receiver);
            if (receiverInfo != null) {
                receiverInfo.netConnection.write(sender + ": " + messageContent);

                // Store message in the database
                storeMessageInDatabase(sender, receiver, messageContent);
            }
        }
    }

    private void storeMessageInDatabase(String sender, String receiver, String content) {
        String query = "INSERT INTO messages (sender_id, receiver_id, content, sent_at) VALUES " +
                "((SELECT user_id FROM users WHERE name = ?), " +
                "(SELECT user_id FROM users WHERE name = ?), ?, ?)";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, content);
            stmt.setObject(4, LocalDateTime.now());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
