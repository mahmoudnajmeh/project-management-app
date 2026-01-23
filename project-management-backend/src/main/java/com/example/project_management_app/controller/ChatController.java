package com.example.project_management_app.controller;


import com.example.project_management_app.dto.ChatMessage;
import com.example.project_management_app.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        try {
            if (principal == null) {
                System.err.println("Principal is null");
                return;
            }

            Authentication auth = (Authentication) principal;
            User user = (User) auth.getPrincipal();

            if (user == null) {
                System.err.println("User is null");
                return;
            }

            System.out.println("Sending message from user: " + user.getUsername() + " (ID: " + user.getId() + ")");
            System.out.println("Message content: " + chatMessage.getContent());
            System.out.println("Receiver ID: " + chatMessage.getReceiverId());

            chatMessage.setSenderId(user.getId());
            chatMessage.setSenderName(user.getUsername());
            chatMessage.setTimestamp(LocalDateTime.now());

            if (chatMessage.getReceiverId() != null) {
                System.out.println("Sending private message to user: " + chatMessage.getReceiverId());
                messagingTemplate.convertAndSendToUser(
                        chatMessage.getReceiverId().toString(),
                        "/queue/messages",
                        chatMessage
                );

                messagingTemplate.convertAndSendToUser(
                        user.getId().toString(),
                        "/queue/messages",
                        chatMessage
                );
            } else if (chatMessage.getRoomId() != null) {
                messagingTemplate.convertAndSend("/topic/room/" + chatMessage.getRoomId(), chatMessage);
            } else {
                messagingTemplate.convertAndSend("/topic/public", chatMessage);
            }

            System.out.println("Message sent successfully");
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.typing")
    public void sendTyping(@Payload ChatMessage chatMessage, Principal principal) {
        try {
            if (principal == null || chatMessage.getReceiverId() == null) {
                return;
            }

            Authentication auth = (Authentication) principal;
            User user;
            user = (User) auth.getPrincipal();

            chatMessage.setSenderId(user.getId());
            chatMessage.setSenderName(user.getUsername());
            chatMessage.setTimestamp(LocalDateTime.now());

            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverId().toString(),
                    "/queue/typing",
                    chatMessage
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.read")
    public void sendReadReceipt(@Payload ChatMessage chatMessage, Principal principal) {
        try {
            if (principal == null || chatMessage.getReceiverId() == null) {
                return;
            }

            Authentication auth = (Authentication) principal;
            User user = (User) auth.getPrincipal();

            chatMessage.setSenderId(user.getId());
            chatMessage.setSenderName(user.getUsername());
            chatMessage.setTimestamp(LocalDateTime.now());

            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverId().toString(),
                    "/queue/read",
                    chatMessage
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}