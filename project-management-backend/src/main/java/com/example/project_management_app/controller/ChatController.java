    package com.example.project_management_app.controller;

    import com.example.project_management_app.dto.ChatMessage;
    import com.example.project_management_app.service.ChatService;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.messaging.handler.annotation.MessageMapping;
    import org.springframework.messaging.handler.annotation.Payload;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.security.core.Authentication;
    import org.springframework.web.bind.annotation.*;

    import java.security.Principal;
    import java.time.LocalDateTime;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.concurrent.ConcurrentHashMap;

    @RestController
    @RequestMapping("/api/chat")
    public class ChatController {

        private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

        private final Map<Long, LocalDateTime> lastMessageTime = new ConcurrentHashMap<>();
        private final Map<Long, LocalDateTime> lastTypingTime = new ConcurrentHashMap<>();
        private static final long MESSAGE_COOLDOWN_MS = 1000;
        private static final long TYPING_COOLDOWN_MS = 2000;

        @Autowired
        private SimpMessagingTemplate messagingTemplate;

        @Autowired
        private ChatService chatService;

        @MessageMapping("/chat.send")
        public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
            try {
                if (principal == null) {
                    logger.warn("Attempt to send message without authentication");
                    return;
                }

                Authentication auth = (Authentication) principal;
                com.example.project_management_app.entity.User user = (com.example.project_management_app.entity.User) auth.getPrincipal();

                LocalDateTime lastTime = lastMessageTime.get(user.getId());
                if (lastTime != null) {
                    long timeSinceLastMessage = java.time.Duration.between(lastTime, LocalDateTime.now()).toMillis();
                    if (timeSinceLastMessage < MESSAGE_COOLDOWN_MS) {
                        logger.warn("Rate limit exceeded for user {}: {}ms since last message",
                                user.getUsername(), timeSinceLastMessage);
                        return;
                    }
                }
                lastMessageTime.put(user.getId(), LocalDateTime.now());

                if (chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
                    logger.warn("Empty message from user {}", user.getUsername());
                    return;
                }

                if (chatMessage.getReceiverId() == null) {
                    logger.warn("Message without receiver from user {}", user.getUsername());
                    return;
                }

                chatMessage.setSenderId(user.getId());
                chatMessage.setSenderName(user.getUsername());
                chatMessage.setTimestamp(LocalDateTime.now());

                ChatMessage savedMessage = chatService.saveMessage(chatMessage);

                messagingTemplate.convertAndSendToUser(
                        chatMessage.getReceiverId().toString(),
                        "/queue/messages",
                        savedMessage
                );

                sendUnreadCountNotification(chatMessage.getReceiverId());

            } catch (Exception e) {
                logger.error("Error sending message: {}", e.getMessage());
            }
        }

        @MessageMapping("/chat.typing")
        public void sendTyping(@Payload ChatMessage chatMessage, Principal principal) {
            try {
                if (principal == null || chatMessage.getReceiverId() == null) {
                    return;
                }

                Authentication auth = (Authentication) principal;
                com.example.project_management_app.entity.User user = (com.example.project_management_app.entity.User) auth.getPrincipal();

                LocalDateTime lastTyping = lastTypingTime.get(user.getId());
                if (lastTyping != null) {
                    long timeSinceLastTyping = java.time.Duration.between(lastTyping, LocalDateTime.now()).toMillis();
                    if (timeSinceLastTyping < TYPING_COOLDOWN_MS) {
                        return;
                    }
                }
                lastTypingTime.put(user.getId(), LocalDateTime.now());

                chatMessage.setSenderId(user.getId());
                chatMessage.setSenderName(user.getUsername());
                chatMessage.setTimestamp(LocalDateTime.now());

                messagingTemplate.convertAndSendToUser(
                        chatMessage.getReceiverId().toString(),
                        "/queue/typing",
                        chatMessage
                );
            } catch (Exception e) {
                logger.error("Error sending typing indicator: {}", e.getMessage());
            }
        }

        @MessageMapping("/chat.read")
        public void sendReadReceipt(@Payload ChatMessage chatMessage, Principal principal) {
            try {
                if (principal == null || chatMessage.getReceiverId() == null) {
                    return;
                }

                Authentication auth = (Authentication) principal;
                com.example.project_management_app.entity.User user = (com.example.project_management_app.entity.User) auth.getPrincipal();

                chatMessage.setSenderId(user.getId());
                chatMessage.setSenderName(user.getUsername());
                chatMessage.setTimestamp(LocalDateTime.now());

                chatService.markMessagesAsRead(user.getId(), chatMessage.getReceiverId());

                messagingTemplate.convertAndSendToUser(
                        chatMessage.getReceiverId().toString(),
                        "/queue/read",
                        chatMessage
                );

                sendUnreadCountNotification(user.getId());

            } catch (Exception e) {
                logger.error("Error sending read receipt: {}", e.getMessage());
            }
        }

        @GetMapping("/conversation/{user1Id}/{user2Id}")
        public ResponseEntity<List<ChatMessage>> getConversation(
                @PathVariable Long user1Id,
                @PathVariable Long user2Id) {
            List<ChatMessage> conversation = chatService.getConversation(user1Id, user2Id);
            return ResponseEntity.ok(conversation);
        }

        @GetMapping("/unread/{userId}")
        public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
            Long count = chatService.getUnreadCount(userId);
            return ResponseEntity.ok(count);
        }

        @GetMapping("/unread/{receiverId}/{senderId}")
        public ResponseEntity<Long> getUnreadCountFromUser(
                @PathVariable Long receiverId,
                @PathVariable Long senderId) {
            Long count = chatService.getUnreadCountFromUser(receiverId, senderId);
            return ResponseEntity.ok(count);
        }

        @PostMapping("/read/{receiverId}/{senderId}")
        public ResponseEntity<?> markAsRead(
                @PathVariable Long receiverId,
                @PathVariable Long senderId) {
            chatService.markMessagesAsRead(receiverId, senderId);
            return ResponseEntity.ok().build();
        }

        @GetMapping("/messages/unread/{userId}")
        public ResponseEntity<List<ChatMessage>> getUnreadMessages(@PathVariable Long userId) {
            List<ChatMessage> messages = chatService.getUnreadMessages(userId);
            return ResponseEntity.ok(messages);
        }

        private void sendUnreadCountNotification(Long userId) {
            Long unreadCount = chatService.getUnreadCount(userId);

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "UNREAD_COUNT");
            notification.put("count", unreadCount);
            notification.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );
        }
    }