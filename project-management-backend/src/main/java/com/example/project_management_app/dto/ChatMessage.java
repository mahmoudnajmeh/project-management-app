package com.example.project_management_app.dto;

import java.time.LocalDateTime;

public class ChatMessage {
    private Long id;
    private MessageType type;
    private String content;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;
    private Long roomId;
    private LocalDateTime timestamp;
    private boolean read;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        TYPING,
        READ_RECEIPT
    }

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(MessageType type, String content, Long senderId, String senderName) {
        this.type = type;
        this.content = content;
        this.senderId = senderId;
        this.senderName = senderName;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}