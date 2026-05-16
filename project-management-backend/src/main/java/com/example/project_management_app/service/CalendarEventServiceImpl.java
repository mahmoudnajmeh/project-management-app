package com.example.project_management_app.service;

import com.example.project_management_app.dto.CalendarEventDto;
import com.example.project_management_app.entity.CalendarEvent;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Team;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.CalendarEventRepository;
import com.example.project_management_app.repository.ProjectRepository;
import com.example.project_management_app.repository.TeamRepository;
import com.example.project_management_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CalendarEventServiceImpl implements CalendarEventService {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

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
        event.setCreator(currentUser);

        if (eventDto.getEventDate() != null && !eventDto.getEventDate().isEmpty()) {
            event.setEventDate(LocalDateTime.parse(eventDto.getEventDate()));
        }

        if (eventDto.getEndDate() != null && !eventDto.getEndDate().isEmpty()) {
            event.setEndDate(LocalDateTime.parse(eventDto.getEndDate()));
        }

        if (event.getColor() == null) {
            event.setColor(getEventColor(event.getType(), event.getPriority()));
        }

        CalendarEvent savedEvent = calendarEventRepository.save(event);

        List<User> teamMembers = userService.getUsersInSameTeam(currentUser);

        String notificationContent = currentUser.getFirstName() + " created calendar event: " + event.getTitle();
        notificationService.createNotificationForUsers(
                teamMembers,
                notificationContent,
                "CALENDAR_EVENT_CREATED",
                savedEvent.getId(),
                "CALENDAR_EVENT",
                currentUser.getId(),
                currentUser.getFirstName() + " " + currentUser.getLastName()
        );

        return savedEvent;
    }

    @Override
    public CalendarEvent updateEvent(Long id, CalendarEventDto eventDto) {
        CalendarEvent event = getEventById(id);
        User currentUser = accountService.getCurrentUser();

        if (!event.getCreator().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the event creator can update this event");
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

        CalendarEvent updatedEvent = calendarEventRepository.save(event);

        List<User> teamMembers = userService.getUsersInSameTeam(currentUser);

        String notificationContent = currentUser.getFirstName() + " updated calendar event: " + event.getTitle();
        notificationService.createNotificationForUsers(
                teamMembers,
                notificationContent,
                "CALENDAR_EVENT_UPDATED",
                event.getId(),
                "CALENDAR_EVENT",
                currentUser.getId(),
                currentUser.getFirstName() + " " + currentUser.getLastName()
        );

        return updatedEvent;
    }

    @Override
    public void deleteEvent(Long id) {
        CalendarEvent event = getEventById(id);
        User currentUser = accountService.getCurrentUser();

        if (!event.getCreator().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the event creator can delete this event");
        }

        List<User> teamMembers = userService.getUsersInSameTeam(currentUser);

        String notificationContent = currentUser.getFirstName() + " deleted calendar event: " + event.getTitle();
        notificationService.createNotificationForUsers(
                teamMembers,
                notificationContent,
                "CALENDAR_EVENT_DELETED",
                event.getId(),
                "CALENDAR_EVENT",
                currentUser.getId(),
                currentUser.getFirstName() + " " + currentUser.getLastName()
        );

        calendarEventRepository.deleteById(id);
    }

    @Override
    public CalendarEvent getEventById(Long id) {
        CalendarEvent event = calendarEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calendar event not found with id: " + id));

        // Fetch creator details
        if (event.getCreator() != null) {
            event.getCreator().getFirstName();
            event.getCreator().getLastName();
            event.getCreator().getProfilePictureFileName();
        }

        return event;
    }

    @Override
    public List<CalendarEvent> getAllEvents() {
        List<CalendarEvent> events = calendarEventRepository.findAll();
        enrichEventsWithCreatorDetails(events);
        return events;
    }

    @Override
    public List<CalendarEvent> getUserEvents() {
        User currentUser = accountService.getCurrentUser();
        List<CalendarEvent> userEvents = calendarEventRepository.findByCreatedBy(currentUser.getId());

        List<Team> userTeams = teamRepository.findByMemberId(currentUser.getId());
        List<CalendarEvent> teamEvents = new ArrayList<>();

        for (Team team : userTeams) {
            List<Project> teamProjects = projectRepository.findByTeam(team);
            for (Project project : teamProjects) {
                List<CalendarEvent> projectEvents = calendarEventRepository.findByProjectId(project.getId());
                teamEvents.addAll(projectEvents);
            }
        }

        Set<CalendarEvent> allEvents = new HashSet<>(userEvents);
        allEvents.addAll(teamEvents);

        List<CalendarEvent> result = new ArrayList<>(allEvents);
        enrichEventsWithCreatorDetails(result);

        return result;
    }

    @Override
    public List<CalendarEvent> getEventsBetween(LocalDateTime start, LocalDateTime end) {
        User currentUser = accountService.getCurrentUser();
        List<CalendarEvent> userEvents = calendarEventRepository.findEventsBetweenDates(currentUser.getId(), start, end);

        List<Team> userTeams = teamRepository.findByMemberId(currentUser.getId());
        List<CalendarEvent> teamEvents = new ArrayList<>();

        for (Team team : userTeams) {
            List<Project> teamProjects = projectRepository.findByTeam(team);
            for (Project project : teamProjects) {
                List<CalendarEvent> projectEvents = calendarEventRepository.findByProjectId(project.getId());
                for (CalendarEvent event : projectEvents) {
                    if ((event.getEventDate().isAfter(start) || event.getEventDate().isEqual(start)) &&
                            (event.getEventDate().isBefore(end) || event.getEventDate().isEqual(end))) {
                        teamEvents.add(event);
                    }
                }
            }
        }

        Set<CalendarEvent> allEvents = new HashSet<>(userEvents);
        allEvents.addAll(teamEvents);

        List<CalendarEvent> result = new ArrayList<>(allEvents);
        enrichEventsWithCreatorDetails(result);

        return result;
    }

    @Override
    public List<CalendarEvent> getEventsByType(String type) {
        User currentUser = accountService.getCurrentUser();
        List<CalendarEvent> userEvents = calendarEventRepository.findByType(currentUser.getId(), type);

        List<Team> userTeams = teamRepository.findByMemberId(currentUser.getId());
        List<CalendarEvent> teamEvents = new ArrayList<>();

        for (Team team : userTeams) {
            List<Project> teamProjects = projectRepository.findByTeam(team);
            for (Project project : teamProjects) {
                List<CalendarEvent> projectEvents = calendarEventRepository.findByProjectId(project.getId());
                for (CalendarEvent event : projectEvents) {
                    if (event.getType().equals(type)) {
                        teamEvents.add(event);
                    }
                }
            }
        }

        Set<CalendarEvent> allEvents = new HashSet<>(userEvents);
        allEvents.addAll(teamEvents);

        List<CalendarEvent> result = new ArrayList<>(allEvents);
        enrichEventsWithCreatorDetails(result);

        return result;
    }

    @Override
    public List<CalendarEvent> getProjectEvents(Long projectId) {
        List<CalendarEvent> events = calendarEventRepository.findByProjectId(projectId);
        enrichEventsWithCreatorDetails(events);
        return events;
    }

    private void enrichEventsWithCreatorDetails(List<CalendarEvent> events) {
        for (CalendarEvent event : events) {
            if (event.getCreator() != null) {
                User creator = event.getCreator();
                creator.getFirstName();
                creator.getLastName();
                creator.getProfilePictureFileName();
                creator.getProfilePictureUrl();
            }
        }
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