//package com.example.project_management_app.repository;
//
//import com.example.project_management_app.entity.ChatMessageEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//
//@Repository
//public interface ChatRepository extends JpaRepository<ChatMessageEntity, Long> {
//    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
//            "(m.senderId = :user1Id AND m.receiverId = :user2Id) OR " +
//            "(m.senderId = :user2Id AND m.receiverId = :user1Id) " +
//            "ORDER BY m.timestamp ASC")
//    List<ChatMessageEntity> findConversation(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
//
//    @Query("SELECT COUNT(m) FROM ChatMessageEntity m WHERE " +
//            "m.receiverId = :userId AND m.senderId = :senderId AND m.read = false")
//    int countUnreadMessages(@Param("userId") Long userId, @Param("senderId") Long senderId);
//
//    List<ChatMessageEntity> findByRoomIdOrderByTimestampAsc(Long roomId);
//}