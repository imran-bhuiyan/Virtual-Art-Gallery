package chat;
//package com.example.test_project;


import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatMessage implements Serializable {
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessage(String sender, String receiver, String content, LocalDateTime timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "From: " + sender + " To: " + receiver + " [" + timestamp + "] : " + content;
    }
}
