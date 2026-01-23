package com.example.project_management_app.service;

import com.example.project_management_app.dto.CalendarEventDto;
import com.example.project_management_app.entity.CalendarEvent;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.CalendarEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CalendarEventServiceImpl implements CalendarEventService {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private AccountService accountService;

    @Override
    public CalendarEvent createEvent(CalendarEventDto eventDto) {
        User currentUser = accountService.getCurrentUser();

        CalendarEvent event = new CalendarEvent();
        event.setTitle(eventDto.getTitle());
        event.setDescription(eventDto.getDescription());
        event.setType(eventDto.getType());
        event.setPriority(eventDto.getPriority());
        event.setStatus(eventDto.getStatus());
        event.setAllDay(eventDto.getAllDay() != null ? eventDto.getAllDay() : false);
        event.setColor(eventDto.getColor());
        event.setProjectId(eventDto.getProjectId());
        event.setAssignedUserId(eventDto.getAssignedUserId());
        event.setCreatedBy(currentUser.getId());

        if (eventDto.getEventDate() != null && !eventDto.getEventDate().isEmpty()) {
            event.setEventDate(LocalDateTime.parse(eventDto.getEventDate()));
        }

        if (eventDto.getEndDate() != null && !eventDto.getEndDate().isEmpty()) {
            event.setEndDate(LocalDateTime.parse(eventDto.getEndDate()));
        }

        if (event.getColor() == null) {
            event.setColor(getEventColor(event.getType(), event.getPriority()));
        }

        return calendarEventRepository.save(event);
    }

    @Override
    public CalendarEvent updateEvent(Long id, CalendarEventDto eventDto) {
        CalendarEvent event = getEventById(id);
        User currentUser = accountService.getCurrentUser();

        if (!event.getCreatedBy().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own events");
        }

        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getType() != null) {
            event.setType(eventDto.getType());
        }
        if (eventDto.getPriority() != null) {
            event.setPriority(eventDto.getPriority());
        }
        if (eventDto.getStatus() != null) {
            event.setStatus(eventDto.getStatus());
        }
        if (eventDto.getEventDate() != null && !eventDto.getEventDate().isEmpty()) {
            event.setEventDate(LocalDateTime.parse(eventDto.getEventDate()));
        }
        if (eventDto.getEndDate() != null && !eventDto.getEndDate().isEmpty()) {
            event.setEndDate(LocalDateTime.parse(eventDto.getEndDate()));
        }
        if (eventDto.getAllDay() != null) {
            event.setAllDay(eventDto.getAllDay());
        }
        if (eventDto.getColor() != null) {
            event.setColor(eventDto.getColor());
        }
        if (eventDto.getProjectId() != null) {
            event.setProjectId(eventDto.getProjectId());
        }
        if (eventDto.getAssignedUserId() != null) {
            event.setAssignedUserId(eventDto.getAssignedUserId());
        }

        return calendarEventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        CalendarEvent event = getEventById(id);
        User currentUser = accountService.getCurrentUser();

        if (!event.getCreatedBy().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own events");
        }

        calendarEventRepository.deleteById(id);
    }

    @Override
    public CalendarEvent getEventById(Long id) {
        return calendarEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calendar event not found with id: " + id));
    }

    @Override
    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    @Override
    public List<CalendarEvent> getUserEvents() {
        User currentUser = accountService.getCurrentUser();
        return calendarEventRepository.findByCreatedBy(currentUser.getId());
    }

    @Override
    public List<CalendarEvent> getEventsBetween(LocalDateTime start, LocalDateTime end) {
        User currentUser = accountService.getCurrentUser();
        return calendarEventRepository.findEventsBetweenDates(currentUser.getId(), start, end);
    }

    @Override
    public List<CalendarEvent> getEventsByType(String type) {
        User currentUser = accountService.getCurrentUser();
        return calendarEventRepository.findByType(currentUser.getId(), type);
    }

    @Override
    public List<CalendarEvent> getProjectEvents(Long projectId) {
        return calendarEventRepository.findByProjectId(projectId);
    }

    private String getEventColor(String type, String priority) {
        switch (type) {
            case "task":
                switch (priority) {
                    case "URGENT": return "#EF4444";
                    case "HIGH": return "#F97316";
                    case "MEDIUM": return "#EAB308";
                    default: return "#3B82F6";
                }
            case "project":
                return "#8B5CF6";
            case "meeting":
                return "#EC4899";
            case "custom":
                return "#14B8A6";
            default:
                return "#6B7280";
        }
    }
}