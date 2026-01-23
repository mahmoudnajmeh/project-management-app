package com.example.project_management_app.controller;

import com.example.project_management_app.dto.CalendarEventDto;
import com.example.project_management_app.entity.CalendarEvent;
import com.example.project_management_app.service.CalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public class CalendarEventController {

    @Autowired
    private CalendarEventService calendarEventService;

    @GetMapping
    public ResponseEntity<List<CalendarEvent>> getAllEvents() {
        List<CalendarEvent> events = calendarEventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/my-events")
    public ResponseEntity<List<CalendarEvent>> getMyEvents() {
        List<CalendarEvent> events = calendarEventService.getUserEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/range")
    public ResponseEntity<List<CalendarEvent>> getEventsInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<CalendarEvent> events = calendarEventService.getEventsBetween(start, end);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CalendarEvent>> getEventsByType(@PathVariable String type) {
        List<CalendarEvent> events = calendarEventService.getEventsByType(type);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CalendarEvent>> getProjectEvents(@PathVariable Long projectId) {
        List<CalendarEvent> events = calendarEventService.getProjectEvents(projectId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarEvent> getEvent(@PathVariable Long id) {
        CalendarEvent event = calendarEventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<CalendarEvent> createEvent(@RequestBody CalendarEventDto eventDto) {
        CalendarEvent event = calendarEventService.createEvent(eventDto);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarEvent> updateEvent(@PathVariable Long id, @RequestBody CalendarEventDto eventDto) {
        CalendarEvent event = calendarEventService.updateEvent(id, eventDto);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable Long id) {
        calendarEventService.deleteEvent(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Event deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<CalendarEvent>> getMonthEvents(
            @PathVariable int year,
            @PathVariable int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);
        List<CalendarEvent> events = calendarEventService.getEventsBetween(start, end);
        return ResponseEntity.ok(events);
    }
}