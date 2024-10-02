package chat;

import java.util.HashMap;
import java.util.List;

public class ReaderWriterServer implements Runnable {

    String username;
    NetworkConnection nc;
    HashMap<String, Information> clientList;

    public ReaderWriterServer(String user, NetworkConnection netConnection, HashMap<String, Information> cList) {
        this.username = user;
        this.nc = netConnection;
        this.clientList = cList;
    }

    @Override
    public void run() {
        while (true) {
            Object obj = nc.read();

            // Check if the object is of type Data (chat message)
            if (obj instanceof Data) {
                Data dataObj = (Data) obj;

                // Save the message to the database using the appropriate fields from Data
                MessageDAO.saveMessage(Integer.parseInt(dataObj.getSenderId()),
                        Integer.parseInt(dataObj.getReceiverId()),
                        dataObj.getMessage(),
                        dataObj.getTimestamp().toString());

                // Find the receiver in the client list and send the message
                Information receiverInfo = clientList.get(dataObj.getReceiverId());
                if (receiverInfo != null) {
                    receiverInfo.netConnection.write(dataObj.getSenderId() + ": " + dataObj.getMessage());
                }
            }

            // Check if the object is a string (could be a request for history)
            else if (obj instanceof String) {
                String actualMessage = (String) obj;

                // Check if this message contains a history request
                if (actualMessage.toLowerCase().contains("history")) {
                    String[] words = actualMessage.split("\\$");
                    // Assuming words[0] and words[1] are the sender and receiver IDs as Strings
                    int senderId = Integer.parseInt(words[0]);
                    int receiverId = Integer.parseInt(words[1]);

                    // Retrieve message history from the database
                    List<Data> history = MessageDAO.getMessageHistory(senderId, receiverId);
                    StringBuilder messageHistory = new StringBuilder("Message History:\n");

                    // Loop through the history and append each message with the timestamp
                    for (Data data : history) {
                        messageHistory.append(data.getTimestamp()).append(" - ")
                                .append(data.getSenderId()).append(": ")
                                .append(data.getMessage()).append("\n");
                    }

                    // Send the history to the client
                    nc.write(messageHistory.toString());
                }
            }
        }
    }
}
