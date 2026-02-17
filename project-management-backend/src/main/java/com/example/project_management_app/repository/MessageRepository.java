package com.example.project_management_app.repository;

import com.example.project_management_app.entity.Message;
import com.example.project_management_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderAndReceiverOrderByTimestampAsc(User sender, User receiver);
    List<Message> findByReceiverAndReadFalse(User receiver);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
            "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.read = false")
    Long countUnreadMessages(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.read = false")
    Long countUnreadMessagesFromUser(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.read = false")
    void markMessagesAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
}