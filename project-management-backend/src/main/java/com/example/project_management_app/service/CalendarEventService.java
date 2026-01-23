package com.example.project_management_app.service;

import com.example.project_management_app.dto.CalendarEventDto;
import com.example.project_management_app.entity.CalendarEvent;
import java.time.LocalDateTime;
import java.util.List;

public interface CalendarEventService {
    CalendarEvent createEvent(CalendarEventDto eventDto);
    CalendarEvent updateEvent(Long id, CalendarEventDto eventDto);
    void deleteEvent(Long id);
    CalendarEvent getEventById(Long id);
    List<CalendarEvent> getAllEvents();
    List<CalendarEvent> getUserEvents();
    List<CalendarEvent> getEventsBetween(LocalDateTime start, LocalDateTime end);
    List<CalendarEvent> getEventsByType(String type);
    List<CalendarEvent> getProjectEvents(Long projectId);
}