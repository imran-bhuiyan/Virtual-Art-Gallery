package chat;

import java.time.LocalDateTime;

public class ChatClient {

    private NetworkConnection nc;
    private String username;
    private String chatPartner;

    public ChatClient(String username, String chatPartner, NetworkConnection nc) {
        this.username = username;
        this.chatPartner = chatPartner;
        this.nc = nc;

        // Retrieve chat history when the chat starts
        String historyRequest = username + "$" + chatPartner + "$history";
        nc.write(historyRequest);
    }

    // Send a message
    public void sendMessage(String message) {
        Data dataToSend = new Data(message, username, chatPartner, LocalDateTime.now());
        nc.write(dataToSend);
    }

    // Display the message history in the chat window
    public void displayMessage(String message) {
        System.out.println("Chat History: \n" + message);
    }
}
