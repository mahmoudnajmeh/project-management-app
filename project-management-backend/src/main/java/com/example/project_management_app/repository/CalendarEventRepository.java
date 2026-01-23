package com.example.project_management_app.repository;

import com.example.project_management_app.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    @Query("SELECT e FROM CalendarEvent e WHERE e.createdBy = :userId")
    List<CalendarEvent> findByCreatedBy(@Param("userId") Long userId);

    @Query("SELECT e FROM CalendarEvent e WHERE (e.eventDate BETWEEN :startDate AND :endDate OR e.endDate BETWEEN :startDate AND :endDate) AND e.createdBy = :userId")
    List<CalendarEvent> findEventsBetweenDates(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM CalendarEvent e WHERE e.type = :type AND e.createdBy = :userId")
    List<CalendarEvent> findByType(@Param("userId") Long userId, @Param("type") String type);

    @Query("SELECT e FROM CalendarEvent e WHERE e.projectId = :projectId AND e.createdBy = :userId")
    List<CalendarEvent> findByProjectId(@Param("userId") Long userId);

    @Query("SELECT e FROM CalendarEvent e WHERE e.eventDate >= :date AND e.createdBy = :userId ORDER BY e.eventDate ASC")
    List<CalendarEvent> findUpcomingEvents(@Param("userId") Long userId, @Param("date") LocalDateTime date);
}