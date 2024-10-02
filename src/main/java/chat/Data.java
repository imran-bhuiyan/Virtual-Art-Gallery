package chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Data {
    private String message;
    private String senderId;
     private String receiverId;
     private LocalDateTime timestamp;

    public Data(String message, String senderId, String receiverId, LocalDateTime timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
}
