package com.example.project_management_app.service;

import com.example.project_management_app.dto.ChatMessage;
import com.example.project_management_app.entity.Message;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.MessageRepository;
import com.example.project_management_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public ChatMessage saveMessage(ChatMessage chatMessageDto) {
        User sender = userRepository.findById(chatMessageDto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(chatMessageDto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = new Message();
        message.setContent(chatMessageDto.getContent());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setType(Message.MessageType.valueOf(chatMessageDto.getType().name()));
        message.setRead(false);

        Message savedMessage = messageRepository.save(message);

        ChatMessage response = convertToDto(savedMessage);
        response.setSenderName(sender.getUsername());
        response.setSenderAvatar(getAvatarUrl(sender));

        if (chatMessageDto.getType() == ChatMessage.MessageType.CHAT) {
            sendNotification(receiver.getId(), "New message from " + sender.getUsername());
        }

        return response;
    }

    public List<ChatMessage> getConversation(Long user1Id, Long user2Id) {
        List<Message> messages = messageRepository.findConversation(user1Id, user2Id);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void markMessagesAsRead(Long receiverId, Long senderId) {
        messageRepository.markMessagesAsRead(receiverId, senderId);
    }

    public Long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    public Long getUnreadCountFromUser(Long receiverId, Long senderId) {
        return messageRepository.countUnreadMessagesFromUser(receiverId, senderId);
    }

    public List<ChatMessage> getUnreadMessages(Long userId) {
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Message> messages = messageRepository.findByReceiverAndReadFalse(receiver);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ChatMessage convertToDto(Message message) {
        ChatMessage dto = new ChatMessage();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getUsername());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());
        dto.setType(ChatMessage.MessageType.valueOf(message.getType().name()));
        dto.setSenderAvatar(getAvatarUrl(message.getSender()));
        return dto;
    }

    private String getAvatarUrl(User user) {
        if (user.getProfilePictureUrl() != null) {
            return user.getProfilePictureUrl();
        } else if (user.getProfilePictureFileName() != null) {
            return "/api/users/profile-picture/" + user.getProfilePictureFileName();
        }
        return null;
    }

    private void sendNotification(Long userId, String message) {
        ChatMessage notification = new ChatMessage();
        notification.setType(ChatMessage.MessageType.NOTIFICATION);
        notification.setContent(message);
        notification.setTimestamp(LocalDateTime.now());

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );
    }
}