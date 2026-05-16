package com.example.project_management_app.controller;

import com.example.project_management_app.dto.ActivityResponseDto;
import com.example.project_management_app.entity.Notification;
import com.example.project_management_app.entity.Project;
import com.example.project_management_app.entity.Task;
import com.example.project_management_app.entity.User;
import com.example.project_management_app.repository.UserRepository;
import com.example.project_management_app.service.AccountService;
import com.example.project_management_app.service.ProjectService;
import com.example.project_management_app.service.TaskService;
import com.example.project_management_app.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/recent")
    public ResponseEntity<List<ActivityResponseDto>> getRecentActivity() {
        User currentUser = accountService.getCurrentUser();
        List<ActivityResponseDto> activities = new ArrayList<>();

        // Get notifications with full user details
        List<Notification> notifications = notificationService.getUserNotifications();
        for (Notification notification : notifications) {
            User sender = null;
            if (notification.getSenderId() != null) {
                sender = userRepository.findById(notification.getSenderId()).orElse(null);
            }
            activities.add(new ActivityResponseDto(notification, sender));
        }

        // Get recent projects (last 30 days)
        List<Project> recentProjects = projectService.getProjectsByUser().stream()
                .filter(p -> p.getUpdatedAt() != null &&
                        p.getUpdatedAt().isAfter(java.time.LocalDateTime.now().minusDays(30)))
                .collect(Collectors.toList());

        for (Project project : recentProjects) {
            String action = "created";
            if (project.getUpdatedAt() != null &&
                    project.getUpdatedAt().isAfter(project.getCreatedAt())) {
                action = "updated";
            }

            activities.add(new ActivityResponseDto(project, action, project.getCreatedBy()));
        }

        // Get recent tasks (last 30 days)
        List<Task> recentTasks = taskService.getTasksByUser().stream()
                .filter(t -> t.getUpdatedAt() != null &&
                        t.getUpdatedAt().isAfter(java.time.LocalDateTime.now().minusDays(30)))
                .collect(Collectors.toList());

        for (Task task : recentTasks) {
            if (task.getAssignedUser() != null) {
                String action = "created";
                if (task.getUpdatedAt() != null &&
                        task.getUpdatedAt().isAfter(task.getCreatedAt())) {
                    if (task.getStatus() == com.example.project_management_app.entity.Task.TaskStatus.DONE) {
                        action = "completed";
                    } else {
                        action = "updated";
                    }
                }

                activities.add(new ActivityResponseDto(task, action, task.getAssignedUser()));
            }
        }

        // Sort by createdAt descending
        activities.sort(Comparator.comparing(ActivityResponseDto::getCreatedAt).reversed());

        return ResponseEntity.ok(activities);
    }

    @GetMapping("/recent/limit/{limit}")
    public ResponseEntity<List<ActivityResponseDto>> getRecentActivityWithLimit(@PathVariable int limit) {
        List<ActivityResponseDto> activities = getRecentActivity().getBody();
        if (activities != null && activities.size() > limit) {
            activities = activities.subList(0, limit);
        }
        return ResponseEntity.ok(activities);
    }
}