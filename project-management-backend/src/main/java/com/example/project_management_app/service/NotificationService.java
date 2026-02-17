package com.example.project_management_app.service;

import com.example.project_management_app.entity.Notification;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.NotificationRepository;
import com.example.project_management_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AccountService accountService;

    public Notification createNotification(User user, String content, String type,
                                           Long entityId, String entityType,
                                           Long senderId, String senderName) {
        Notification notification = new Notification(user, content, type,
                entityId, entityType,
                senderId, senderName);
        Notification savedNotification = notificationRepository.save(notification);

        sendRealTimeNotification(user.getId(), savedNotification);
        sendUnreadCountNotification(user.getId());

        return savedNotification;
    }

    public void createNotificationForUsers(List<User> users, String content, String type,
                                           Long entityId, String entityType,
                                           Long senderId, String senderName) {
        for (User user : users) {
            if (!user.getId().equals(senderId)) {
                createNotification(user, content, type, entityId, entityType, senderId, senderName);
            }
        }
    }

    public List<Notification> getUserNotifications() {
        User currentUser = accountService.getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser);
    }

    public List<Notification> getUnreadNotifications() {
        User currentUser = accountService.getCurrentUser();
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(currentUser);
    }

    public Long getUnreadCount() {
        User currentUser = accountService.getCurrentUser();
        return notificationRepository.countUnreadByUser(currentUser);
    }

    public void markAsRead(Long notificationId) {
        User currentUser = accountService.getCurrentUser();
        notificationRepository.markAsRead(currentUser, notificationId);
        sendUnreadCountNotification(currentUser.getId());
    }

    public void markAllAsRead() {
        User currentUser = accountService.getCurrentUser();
        notificationRepository.markAllAsRead(currentUser);
        sendUnreadCountNotification(currentUser.getId());
    }

    public void deleteReadNotifications() {
        User currentUser = accountService.getCurrentUser();
        notificationRepository.deleteReadByUser(currentUser);
    }

    public void deleteAllNotifications() {
        User currentUser = accountService.getCurrentUser();
        notificationRepository.deleteAllByUser(currentUser);
        sendUnreadCountNotification(currentUser.getId());
    }

    private void sendRealTimeNotification(Long userId, Notification notification) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("id", notification.getId());
        notificationData.put("content", notification.getContent());
        notificationData.put("type", notification.getType());
        notificationData.put("entityId", notification.getEntityId());
        notificationData.put("entityType", notification.getEntityType());
        notificationData.put("read", notification.isRead());
        notificationData.put("createdAt", notification.getCreatedAt());
        notificationData.put("senderId", notification.getSenderId());
        notificationData.put("senderName", notification.getSenderName());
        notificationData.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notificationData
        );
    }

    private void sendUnreadCountNotification(Long userId) {
        Long unreadCount = notificationRepository.countUnreadByUser(
                userRepository.findById(userId).orElse(null)
        );

        Map<String, Object> countNotification = new HashMap<>();
        countNotification.put("type", "UNREAD_COUNT_UPDATE");
        countNotification.put("unreadCount", unreadCount);
        countNotification.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                countNotification
        );
    }
}