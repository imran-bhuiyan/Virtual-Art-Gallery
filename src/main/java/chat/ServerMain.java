package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {

    private static final int PORT = 5555;
    private static List<Information> usersList = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                // Start a new thread for each connected user
                new Thread(new UserHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void addUser(Information userInfo) {
        usersList.add(userInfo);
    }

    public static synchronized void removeUser(Information userInfo) {
        usersList.remove(userInfo);
    }

    public static synchronized Information getUserByUsername(String username) {
        for (Information info : usersList) {
            if (info.username.equals(username)) {
                return info;
            }
        }
        return null;
    }
}
