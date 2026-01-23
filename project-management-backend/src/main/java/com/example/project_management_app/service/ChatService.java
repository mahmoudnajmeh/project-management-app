package com.example.project_management_app.service;

import com.example.project_management_app.dto.ChatMessage;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private final Map<Long, List<ChatMessage>> userMessages = new HashMap<>();
    private final Map<String, List<ChatMessage>> roomMessages = new HashMap<>();
    private long messageIdCounter = 1;

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        chatMessage.setId(messageIdCounter++);

        if (chatMessage.getReceiverId() != null) {
            savePrivateMessage(chatMessage);
        } else if (chatMessage.getRoomId() != null) {
            saveRoomMessage(chatMessage);
        }

        return chatMessage;
    }

    private void savePrivateMessage(ChatMessage chatMessage) {
        Long senderId = chatMessage.getSenderId();
        Long receiverId = chatMessage.getReceiverId();

        userMessages.computeIfAbsent(senderId, k -> new ArrayList<>()).add(chatMessage);
        userMessages.computeIfAbsent(receiverId, k -> new ArrayList<>()).add(chatMessage);
    }

    private void saveRoomMessage(ChatMessage chatMessage) {
        String roomKey = "room_" + chatMessage.getRoomId();
        roomMessages.computeIfAbsent(roomKey, k -> new ArrayList<>()).add(chatMessage);
    }

    public List<ChatMessage> getPrivateMessages(Long userId1, Long userId2) {
        List<ChatMessage> allMessages = new ArrayList<>();

        List<ChatMessage> user1Messages = userMessages.getOrDefault(userId1, new ArrayList<>());
        List<ChatMessage> user2Messages = userMessages.getOrDefault(userId2, new ArrayList<>());

        for (ChatMessage msg : user1Messages) {
            if ((msg.getSenderId().equals(userId1) && msg.getReceiverId().equals(userId2)) ||
                    (msg.getSenderId().equals(userId2) && msg.getReceiverId().equals(userId1))) {
                allMessages.add(msg);
            }
        }

        for (ChatMessage msg : user2Messages) {
            if ((msg.getSenderId().equals(userId1) && msg.getReceiverId().equals(userId2)) ||
                    (msg.getSenderId().equals(userId2) && msg.getReceiverId().equals(userId1))) {
                if (!allMessages.contains(msg)) {
                    allMessages.add(msg);
                }
            }
        }

        allMessages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        return allMessages;
    }

    public List<ChatMessage> getRoomMessages(Long roomId) {
        String roomKey = "room_" + roomId;
        return roomMessages.getOrDefault(roomKey, new ArrayList<>());
    }

    public void markMessagesAsRead(Long senderId, Long receiverId) {
        List<ChatMessage> messages = userMessages.getOrDefault(receiverId, new ArrayList<>());
        for (ChatMessage msg : messages) {
            if (msg.getSenderId().equals(senderId) && msg.getReceiverId().equals(receiverId)) {
                msg.setRead(true);
            }
        }
    }

    public int getUnreadCount(Long userId, Long senderId) {
        List<ChatMessage> messages = userMessages.getOrDefault(userId, new ArrayList<>());
        int count = 0;
        for (ChatMessage msg : messages) {
            if (msg.getSenderId().equals(senderId) && msg.getReceiverId().equals(userId) && !msg.isRead()) {
                count++;
            }
        }
        return count;
    }
}